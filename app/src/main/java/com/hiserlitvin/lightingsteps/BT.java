package com.hiserlitvin.lightingsteps;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public abstract class BT extends BluetoothGattCallback
{
	static private String TAG = "BT";
	static private int TIMEOUT_SCAN = 10;
	static private String DEVICE_NAME = "LightingSteps1.0";
	static private UUID PRIMARY_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
	static private UUID CHARACTERISTIC_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
	
	private BluetoothAdapter adapter;
	private BluetoothLeScanner search;
	private Context context;
	private ScanCallback scanCallback;
	private BluetoothGatt gatt;
	private BluetoothGattCharacteristic ch;
	private Thread timeoutScan;
	private List<byte[]> queryList;
	private Thread querySend;
	
	public enum Command
	{
		GetParam,
		GetValue,
		SetValue,
		GetState,
		Save,
		Default,
		GetValueAll,
		GetStateAll,
		Unknown
	}
	
	public enum Failed
	{
		ScanTimeout,
		ScanError,
		IOError,
		InternalError
	}
	
	public abstract void onCreate();
	public abstract void onDestroy();
	public abstract void onFound(BluetoothDevice device);
	public abstract void onNoAdapter();
	public abstract void onFailed(Failed code);
	public abstract void onConnected();
	public abstract void onDisConnected();
	public abstract void onReceived(Command command, byte[] data);
	
	public BT(Context context)
	{
		this.context = context;
	}
	
	public void Create()
	{
		if (adapter != null) return;
		adapter = BluetoothAdapter.getDefaultAdapter();
		if (adapter == null)
		{
			Log.d(TAG,"no adapter");
			onNoAdapter();
		}
		else if (!adapter.isEnabled())
		{
			Log.d(TAG, "disabled");
			onNoAdapter();
		}
		else
		{
			search = adapter.getBluetoothLeScanner();
			if (search == null)
			{
				onNoAdapter();
			}
			else
			{
				scanCallback = new ScanCallback()
				{
					@Override
					public void onScanResult(int callbackType, ScanResult result)
					{
						if (timeoutScan != null) timeoutScan.interrupt();
						if (scanCallback != null)
						{
							search.stopScan(scanCallback);
							BluetoothDevice d = result.getDevice();
							Log.d(TAG, "found"
									+ "\n\tname:\t\t" + d.getName()
									+ "\n\taddress:\t" + d.getAddress()
									+ "\n\tclass:\t\t" + d.getBluetoothClass().toString());
							((Activity) context).runOnUiThread(new Runnable()
							{
								@Override
								public void run()
								{
									onFound(d);
								}
							});
						}
					}
					
					@Override
					public void onBatchScanResults(List<ScanResult> results)
					{
						if (results.size() > 0) onScanResult(ScanSettings.CALLBACK_TYPE_FIRST_MATCH, results.get(0));
					}
					
					@Override
					public void onScanFailed(int errorCode)
					{
						if (timeoutScan != null) timeoutScan.interrupt();
						if (scanCallback != null)
						{
							search.stopScan(scanCallback);
							Log.d(TAG, "scan failed " + errorCode);
							((Activity) context).runOnUiThread(new Runnable()
							{
								@Override
								public void run()
								{
									onFailed(Failed.ScanError);
								}
							});
						}
					}
				};
				timeoutScan = new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							Thread.sleep(TIMEOUT_SCAN * 1000);
							if (scanCallback != null) search.stopScan(scanCallback);
							((Activity) context).runOnUiThread(new Runnable()
							{
								@Override
								public void run()
								{
									onFailed(Failed.ScanTimeout);
								}
							});
						} catch (InterruptedException ex) { }
					}
				});
				onCreate();
			}
		}
	}
	
	public void Destroy()
	{
		if (adapter == null) return;
		if (search != null) onDestroy();
		if (scanCallback != null) search.stopScan(scanCallback);
		if (timeoutScan != null) timeoutScan.interrupt();
		close();
		scanCallback = null;
		timeoutScan = null;
		search = null;
		adapter = null;
	}
	
	private void clean()
	{
		if (querySend != null)
		{
			querySend.interrupt();
			querySend = null;
			queryList = null;
		}
		if (gatt != null) gatt.close();
		gatt = null;
		ch = null;
	}
	
	private void fail()
	{
		clean();
		((Activity)context).runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				onFailed(Failed.IOError);
			}
		});
	}
	
	public void startScan()
	{
		if (search == null)
		{
			Log.d(TAG, "search is null");
			onFailed(Failed.InternalError);
		}
		else
		{
			ScanSettings ss = new ScanSettings.Builder()
					.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
					.setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH)
					.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
					.setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
					.setReportDelay(0)
					.build();
			List<ScanFilter> sf = new ArrayList<>();
			sf.add(new ScanFilter.Builder().setDeviceName(DEVICE_NAME).build());
			search.startScan(sf, ss, scanCallback);
			timeoutScan.start();
			Log.d(TAG, "start scan");
		}
	}
	
	public void connect(BluetoothDevice device)
	{
		if (gatt != null || querySend != null) return;
		queryList = new ArrayList<>();
		querySend = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				boolean loop = true;
				while (loop)
				{
					try
					{
						Thread.sleep(100);
					}
					catch (InterruptedException e)
					{
						loop = false;
					}
					if (ch != null && queryList != null && queryList.size() > 0)
					{
						byte[] buffer = queryList.get(0);
						queryList.remove(0);
						ch.setValue(buffer);
						gatt.writeCharacteristic(ch);
					}
				}
			}
		});
		gatt = device.connectGatt(context, true, new BluetoothGattCallback()
		{
			@Override
			public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
			{
				if (newState == BluetoothGatt.STATE_CONNECTED)
				{
					if (status == BluetoothGatt.GATT_SUCCESS)
					{
						if (!gatt.discoverServices())
						{
							Log.d(TAG, "fail discovery");
							fail();
						}
					}
					else
					{
						Log.d(TAG, "error connect");
						fail();
					}
				}
				else if (newState == BluetoothGatt.STATE_DISCONNECTED)
				{
					Log.d(TAG, "disconnected");
					clean();
					((Activity)context).runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							onDisConnected();
						}
					});
				}
			}
			
			@Override
			public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
			{
				byte[] p = characteristic.getValue();
				if (p.length >= 4 && p[0] == 0x5A && p[1] == p.length && p[2] == crc(p, p[2]))
				{
					Command c;
					switch (p[3])
					{
						case 0: c = Command.GetParam; break;
						case 1: c = Command.GetValue; break;
						case 2: c = Command.SetValue; break;
						case 3: c = Command.GetState; break;
						case 4: c = Command.Save; break;
						case 5: c = Command.Default; break;
						case 6: c = Command.GetValueAll; break;
						case 7: c = Command.GetStateAll; break;
						default: c = Command.Unknown; break;
					}
					byte[] d = Arrays.copyOfRange(p, 4, p.length);
					((Activity)context).runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							onReceived(c, d);
						}
					});
				}
				else
				{
					Log.d(TAG, "bad packet " + p.length);
					String s = "Hex: ";
					for (byte b : p) s += String.format("%x ", b);
					Log.d(TAG, s);
				}
			}
			
			@Override
			public void onServicesDiscovered(BluetoothGatt gatt, int status)
			{
				if (status == BluetoothGatt.GATT_FAILURE)
				{
					Log.d(TAG, "error discovered");
					fail();
				}
				else
				{
					ch = gatt.getService(PRIMARY_UUID).getCharacteristic(CHARACTERISTIC_UUID);
					if (!gatt.setCharacteristicNotification(ch, true))
					{
						Log.d(TAG, "fail characteristic");
						fail();
					}
					else
					{
						((Activity)context).runOnUiThread(new Runnable()
						{
							@Override
							public void run()
							{
								onConnected();
							}
						});
						querySend.start();
					}
				}
			}
		});
	}
	
	public void close()
	{
		clean();
	}
	
	public void query(Command command)
	{
		query(command, new byte[0]);
	}
	public void query(Command command, byte[] data)
	{
		if (queryList != null)
		{
			byte[] buffer = new byte[data.length + 4];
			buffer[0] = 0x5A;
			buffer[1] = (byte)buffer.length;
			buffer[2] = 0;
			switch (command)
			{
				case GetParam: buffer[3] = 0; break;
				case GetValue: buffer[3] = 1; break;
				case SetValue: buffer[3] = 2; break;
				case GetState: buffer[3] = 3; break;
				case Save: buffer[3] = 4; break;
				case Default: buffer[3] = 5; break;
				case GetValueAll: buffer[3] = 6; break;
				case GetStateAll: buffer[3] = 7; break;
				
				case Unknown:
				default:
					buffer[3] = (byte)0xFF;
					break;
			}
			System.arraycopy(data, 0, buffer, 4, data.length);
			buffer[2] = crc(buffer);
			
			if (queryList.size() >= 10)
			{
				Log.d(TAG, "queryList overflow");
			}
			else
			{
				queryList.add(buffer);
			}
			/*ch.setValue(buffer);
			gatt.writeCharacteristic(ch);*/
		}
	}
	
	private byte crc(byte[] data)
	{
		return crc(data, (byte)0x00);
	}
	
	private byte crc(byte[] data, byte decCRC)
	{
		int crc = 0x5A;
		for (byte b : data)
		{
			crc += (0xFF & b);
		}
		crc -= (0xFF & decCRC);
		return (byte)(0xFF & crc);
	}
	
	public enum Param
	{
		ChannelCount,
		StandbyLevel,
		ActiveLevel,
		Sonic1Value,
		Sonic2Value,
		WorkTime,
		AnimationTime,
		AnimationType,
		SwitchType,
		PlayBackType,
		SensorType,
		Unknown
	}
	
	public enum State
	{
		Sonic1,
		Sonic2,
		Sensor1,
		Sensor2,
		Day,
		Switch,
		Unknown
	}
	
	private byte Param2byte(Param param)
	{
		byte p;
		switch (param)
		{
			case ChannelCount: p = 0; break;
			case StandbyLevel: p = 1; break;
			case ActiveLevel: p = 2; break;
			case Sonic1Value: p = 3; break;
			case Sonic2Value: p = 4; break;
			case WorkTime: p = 5; break;
			case AnimationTime: p = 6; break;
			case AnimationType: p = 7; break;
			case SwitchType: p = 8; break;
			case PlayBackType: p = 9; break;
			case SensorType: p = 10; break;
			default: p = (byte)0xFF;
		}
		return p;
	}
	
	private Param byte2Param(byte param)
	{
		Param p;
		switch (param)
		{
			case 0: p = Param.ChannelCount; break;
			case 1: p = Param.StandbyLevel; break;
			case 2: p = Param.ActiveLevel; break;
			case 3: p = Param.Sonic1Value; break;
			case 4: p = Param.Sonic2Value; break;
			case 5: p = Param.WorkTime; break;
			case 6: p = Param.AnimationTime; break;
			case 7: p = Param.AnimationType; break;
			case 8: p = Param.SwitchType; break;
			case 9: p = Param.PlayBackType; break;
			case 10: p = Param.SensorType; break;
			default: p = Param.Unknown; break;
		}
		return p;
	}
	
	private byte State2byte(State state)
	{
		byte p;
		switch (state)
		{
			case Sonic1: p = 0; break;
			case Sonic2: p = 1; break;
			case Sensor1: p = 2; break;
			case Sensor2: p = 3; break;
			case Day: p = 4; break;
			case Switch: p = 5; break;
			default: p = (byte)0xFF;
		}
		return p;
	}
	
	private State byte2State(byte state)
	{
		State p;
		switch (state)
		{
			case 0: p = State.Sonic1; break;
			case 1: p = State.Sonic2; break;
			case 2: p = State.Sensor1; break;
			case 3: p = State.Sensor2; break;
			case 4: p = State.Day; break;
			case 5: p = State.Switch; break;
			default: p = State.Unknown; break;
		}
		return p;
	}
	
	public byte[] GetParamQuery(Param param)
	{
		byte[] d = new byte[1];
		d[0] = Param2byte(param);
		return d;
	}
	
	public class GetParamRequest
	{
		public Param index;
		public int value;
		public int min;
		public int max;
		public int def;
		
		public GetParamRequest(byte[] data)
		{
			if (data.length == 5)
			{
				index = byte2Param(data[0]);
				value = data[1];
				min = data[2];
				max = data[3];
				def = data[4];
			}
			else
			{
				throw new RuntimeException("GetParamRequest failed size " + data.length);
			}
		}
	}
	
	public class GetStateAllRequest
	{
		public int Sonic1;
		public int Sonic2;
		public boolean Sensor1;
		public boolean Sensor2;
		public boolean isDay;
		public boolean isSwitch;
		
		public GetStateAllRequest(byte[] data)
		{
			if (data.length == 6)
			{
				Sonic1 = data[0];
				Sonic2 = data[1];
				Sensor1 = (data[2] != 0);
				Sensor2 = (data[3] != 0);
				isDay = (data[4] != 0);
				isSwitch = (data[5] != 0);
			}
			else
			{
				throw new RuntimeException("GetStateAllRequest failed size " + data.length);
			}
		}
	}
	
	public byte[] SetValueQuery(int index, int value)
	{
		byte[] d = new byte[2];
		d[0] = (byte)index;
		d[1] = (byte)value;
		return d;
	}
	
	public class SetValueRequest
	{
		public int index;
		public int value;
		
		public SetValueRequest(byte[] data)
		{
			if (data.length == 2)
			{
				index = 0xFF & data[0];
				value = data[1];
			}
			else
			{
				throw new RuntimeException("SetValueRequest failed size " + data.length);
			}
		}
	}
}
