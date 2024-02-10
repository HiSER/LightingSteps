package com.hiserlitvin.lightingsteps;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Timer;

public class pageConfig extends myFragment
{
	private View view;
	private OnChangeListener change;
	private Timer changed;
	
	public pageConfig(Context context)
	{
		super(context);
	}
	
	public CharSequence getTitle()
	{
		return context.getString(R.string.page_config);
	}
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		view = inflater.inflate(R.layout.page_config, container, false);
		mySeekBar.OnValueListener sc = new mySeekBar.OnValueListener()
		{
			@Override
			public void onValueChanged(mySeekBar s, int value, int mulValue)
			{
				Toast.makeText(getContext(), "" + mulValue, Toast.LENGTH_SHORT).show();
			}
			
			@Override
			public void onValueHolded(mySeekBar s, int value, int mulValue)
			{
				s.setEnabled(false);
				if (change != null) change.onChangeValue(Integer.parseInt(s.getTag().toString()), value);
			}
		};
		
		myListView.OnValueListener ic = new myListView.OnValueListener()
		{
			@Override
			public void onValueChanged(myListView s, int value)
			{
				if (!s.isEnabled()) return;
				s.setEnabled(false);
				if (change != null) change.onChangeValue(Integer.parseInt(s.getTag().toString()), value);
			}
		};
		
		mySeekBar sb;
		
		sb = (mySeekBar) view.findViewById(R.id.seek_0);
		sb.setRange(4, 24);
		sb.setValue(5);
		sb.setOnValueListener(sc);
		
		sb = (mySeekBar) view.findViewById(R.id.seek_1);
		sb.setRange(0, 10);
		sb.setValue(1);
		sb.setOnValueListener(sc);
		
		sb = (mySeekBar) view.findViewById(R.id.seek_2);
		sb.setRange(0, 10);
		sb.setValue(3);
		sb.setOnValueListener(sc);
		
		sb = (mySeekBar) view.findViewById(R.id.seek_3);
		sb.setRange(0, 20);
		sb.setValue(1);
		sb.setOnValueListener(sc);
		
		sb = (mySeekBar) view.findViewById(R.id.seek_4);
		sb.setRange(0, 20);
		sb.setValue(1);
		sb.setOnValueListener(sc);
		
		sb = (mySeekBar) view.findViewById(R.id.seek_5);
		sb.setRange(1, 6, 5);
		sb.setValue(1);
		sb.setOnValueListener(sc);
		
		sb = (mySeekBar) view.findViewById(R.id.seek_6);
		sb.setRange(1, 20, 100);
		sb.setValue(1);
		sb.setOnValueListener(sc);
		
		myListView lv ;
		
		lv = (myListView) view.findViewById(R.id.list_7);
		lv.setList(context, R.array.param_7_list);
		lv.setValue(0);
		lv.setOnValueListener(ic);
		
		lv = (myListView) view.findViewById(R.id.list_8);
		lv.setList(context, R.array.param_8_list);
		lv.setValue(0);
		lv.setOnValueListener(ic);
		
		lv = (myListView) view.findViewById(R.id.list_9);
		lv.setList(context, R.array.param_9_list);
		lv.setValue(1);
		lv.setOnValueListener(ic);
		
		lv = (myListView) view.findViewById(R.id.list_10);
		lv.setList(context, R.array.param_10_list);
		lv.setValue(0);
		lv.setOnValueListener(ic);
		
		return view;
	}
	
	public void Disable()
	{
		if (view != null)
		{
			view.findViewById(R.id.config).setVisibility(View.INVISIBLE);
		}
	}
	
	public void Enable()
	{
		if (view != null)
		{
			view.findViewById(R.id.config).setVisibility(View.VISIBLE);
		}
	}
	
	private int getIdView(int index)
	{
		int id;
		switch (index)
		{
			case 0: id = R.id.seek_0; break;
			case 1: id = R.id.seek_1; break;
			case 2: id = R.id.seek_2; break;
			case 3: id = R.id.seek_3; break;
			case 4: id = R.id.seek_4; break;
			case 5: id = R.id.seek_5; break;
			case 6: id = R.id.seek_6; break;
			case 7: id = R.id.list_7; break;
			case 8: id = R.id.list_8; break;
			case 9: id = R.id.list_9; break;
			case 10: id = R.id.list_10; break;
			default: id = 0; break;
		}
		return id;
	}
	
	public void setValue(int index, int value)
	{
		int id = getIdView(index);
		if (id != 0)
		{
			View v = view.findViewById(id);
			v.setEnabled(false);
			if (index < 7)
			{
				((mySeekBar)v).setValue(value);
			}
			else
			{
				((myListView)v).setValue(value);
			}
			v.setEnabled(true);
		}
	}
	
	public void setOnChangeListener(OnChangeListener change)
	{
		this.change = change;
	}
	
	public interface OnChangeListener
	{
		public void onChangeValue(int index, int value);
	}
}
