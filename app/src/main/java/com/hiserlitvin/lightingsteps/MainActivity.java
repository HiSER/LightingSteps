package com.hiserlitvin.lightingsteps;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends FragmentActivity
{
	static private final String TAG = "LS";
	
	private pageDevice pDevice;
	private pageConfig pConfig;
	private fragmentLayout fNoAdapter;
	private fragmentLayout fFailed;
	private fragmentLayout fScan;
	private fragmentConnected fConnected;
	private fragmentLayout fConnect;
	private fragmentLayout fDisConnected;
	private fragmentNoGranted fNoGranted;
	private BT bt;
	private boolean granted = true;
	private boolean attached = false;
	

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		granted = isGranted();
		
		pDevice = new pageDevice(this);
		pDevice.setOnListener(new pageDevice.onListener()
		{
			@Override
			public void onAttach()
			{
				Log.d(TAG, "pageDevice attach");
				if (granted)
				{
					if (!attached) bt.Create();
				}
				else
				{
					pDevice.setFragment(fNoGranted);
				}
				attached = true;
			}
		});
		
		pConfig = new pageConfig(this);
		pConfig.setOnChangeListener(new pageConfig.OnChangeListener()
		{
			@Override
			public void onChangeValue(int index, int value)
			{
				bt.query(BT.Command.SetValue, bt.SetValueQuery(index, value));
			}
		});
		
		List<myFragment> fragments = new ArrayList<>();
		fragments.add(pDevice);
		fragments.add(pConfig);
		myFragmentPagerAdapter fpa = new myFragmentPagerAdapter(fragments, getSupportFragmentManager());
		ViewPager pager = (ViewPager)findViewById(R.id.pager);
		pager.setAdapter(new myFragmentPagerAdapter(fragments, getSupportFragmentManager()));
		pager.setOffscreenPageLimit(fpa.getCount());
		
		fNoAdapter = new fragmentLayout(R.layout.frm_device_noadapter);
		fFailed = new fragmentLayout(R.layout.frm_device_failed);
		fScan = new fragmentLayout(R.layout.frm_device_scan);
		fConnect = new fragmentLayout(R.layout.frm_device_connect);
		fDisConnected = new fragmentLayout(R.layout.frm_device_disconnected);
		fNoGranted = new fragmentNoGranted(this);
		fNoGranted.setOnClickListener(new fragmentNoGranted.OnClickListener()
		{
			@Override
			public void onGrantQuery()
			{
				requestPermissions(new String[]
					{
						Manifest.permission.BLUETOOTH,
						Manifest.permission.BLUETOOTH_ADMIN,
						Manifest.permission.ACCESS_COARSE_LOCATION,
						Manifest.permission.ACCESS_FINE_LOCATION
					}, 1);
			}
		});
		fConnected = new fragmentConnected(this);
		fConnected.setOnClickListener(new fragmentConnected.OnClickListener()
		{
			@Override
			public void onSave()
			{
				bt.query(BT.Command.Save);
			}
			
			@Override
			public void onDefault()
			{
				bt.query(BT.Command.Default);
			}
		});
		
		bt = new BT(this)
		{
			private Timer stateQuery;
			
			@Override
			public void onCreate()
			{
				pDevice.setFragment(fScan);
				startScan();
				stateQuery = new Timer();
				pager.setCurrentItem(0, true);
			}
			
			@Override
			public void onDestroy()
			{
				stateQuery.cancel();
				stateQuery = null;
			}
			
			@Override
			public void onFound(BluetoothDevice device)
			{
				pDevice.setFragment(fConnect);
				connect(device);
				pager.setCurrentItem(0, true);
			}
			
			@Override
			public void onFailed(Failed code)
			{
				Log.d(TAG, "bt failed " + code.toString());
				pDevice.setFragment(fFailed);
				pConfig.Disable();
				pager.setCurrentItem(0, true);
			}
			
			@Override
			public void onNoAdapter()
			{
				pDevice.setFragment(fNoAdapter);
				pConfig.Disable();
				pager.setCurrentItem(0, true);
			}
			
			@Override
			public void onConnected()
			{
				Log.d(TAG, "bt connected");
				pDevice.setFragment(fConnected);
				pConfig.Enable();
				query(Command.GetValueAll);
				stateQuery.schedule(new TimerTask()
				{
					@Override
					public void run()
					{
						query(Command.GetStateAll);
					}
				}, 0, 300);
			}
			
			@Override
			public void onDisConnected()
			{
				Log.d(TAG, "bt disconnected");
				pDevice.setFragment(fDisConnected);
				pConfig.Disable();
				pager.setCurrentItem(0, true);
			}
			
			@Override
			public void onReceived(Command command, byte[] data)
			{
				switch (command)
				{
					case SetValue:
						BT.SetValueRequest v =  new BT.SetValueRequest(data);
						pConfig.setValue(v.index, v.value);
						if (v.index == 0) fConnected.setChannelsCount(v.value);
						break;
					case Save:
						fConnected.setSaved();
						break;
					case Default:
						query(Command.GetValueAll);
						fConnected.setDefaults();
						break;
					case GetValueAll:
						int i;
						for (i = 0; i < data.length; i++) pConfig.setValue(i, data[i]);
						fConnected.setChannelsCount(data[0]);
						break;
					case GetStateAll:
						BT.GetStateAllRequest s =  new BT.GetStateAllRequest(data);
						fConnected.setSonic1(s.Sonic1);
						fConnected.setSonic2(s.Sonic2);
						fConnected.setSensor1(s.Sensor1);
						fConnected.setSensor2(s.Sensor2);
						fConnected.setDay(s.isDay);
						fConnected.setSwitch(s.isSwitch);
						break;
					default:
						Log.d(TAG, "recv ignore " + command.toString() + " (" + data.length + ")");
						break;
				}
			}
		};
		
	}
	
	private boolean isGranted()
	{
		return (checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
				&& checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
				&& checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
				&& checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		granted = isGranted();
		if (granted) bt.Create();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		if (granted && attached) bt.Create();
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		pConfig.Disable();
		bt.Destroy();
	}
}
