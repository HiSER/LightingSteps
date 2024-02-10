package com.hiserlitvin.lightingsteps;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

public class fragmentConnected extends Fragment
{
	private Context context;
	private View view;
	private OnClickListener click;
	
	public fragmentConnected(Context context)
	{
		this.context = context;
	}
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		view = inflater.inflate(R.layout.frm_device_connected, container, false);
		((Button)view.findViewById(R.id.state_save)).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				v.setEnabled(false);
				if (click != null) click.onSave();
			}
		});
		((Button)view.findViewById(R.id.state_def)).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				v.setEnabled(false);
				if (click != null) click.onDefault();
			}
		});
		return view;
	}
	
	public void setSonic1(int value)
	{
		((TextView)view.findViewById(R.id.state_0)).setText("" + value);
	}
	
	public void setSonic2(int value)
	{
		((TextView)view.findViewById(R.id.state_1)).setText("" + value);
	}
	
	public void setSensor1(boolean value)
	{
		((SwitchCompat)view.findViewById(R.id.state_2)).setChecked(value);
	}
	
	public void setSensor2(boolean value)
	{
		((SwitchCompat)view.findViewById(R.id.state_3)).setChecked(value);
	}
	
	public void setDay(boolean value)
	{
		((SwitchCompat)view.findViewById(R.id.state_4)).setChecked(value);
	}
	
	public void setSwitch(boolean value)
	{
		((SwitchCompat)view.findViewById(R.id.state_5)).setChecked(value);
	}
	
	public void setChannelsCount(int value)
	{
		((TextView)view.findViewById(R.id.param_0_info)).setText("" + value);
	}
	
	public void setSaved()
	{
		view.findViewById(R.id.state_save).setEnabled(true);
		Toast.makeText(context, R.string.cfg_saved, Toast.LENGTH_SHORT).show();
	}
	
	public void setDefaults()
	{
		view.findViewById(R.id.state_def).setEnabled(true);
		Toast.makeText(context, R.string.cfg_defaults, Toast.LENGTH_SHORT).show();
	}
	
	public final void setOnClickListener(OnClickListener click)
	{
		this.click = click;
	}
	
	public interface OnClickListener
	{
		void onSave();
		void onDefault();
	}
}
