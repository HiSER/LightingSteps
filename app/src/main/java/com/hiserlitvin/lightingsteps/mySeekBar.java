package com.hiserlitvin.lightingsteps;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;
import androidx.appcompat.widget.AppCompatSeekBar;

public class mySeekBar extends AppCompatSeekBar
{
	private int __min;
	private int __mul;
	private OnValueListener __l;
	
	public mySeekBar(Context context)
	{
		super(context);
		__init();
	}
	public mySeekBar(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		__init();
	}
	public mySeekBar(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		__init();
	}
	
	private final void __init()
	{
		OnSeekBarChangeListener sc = new OnSeekBarChangeListener()
		{
			private int v = 0;
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b)
			{
				v = i + __min;
				if (__l != null && isEnabled()) __l.onValueChanged((mySeekBar)seekBar, v, v * __mul);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) { }
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				if (__l != null && isEnabled()) __l.onValueHolded((mySeekBar)seekBar, v, v * __mul);
			}
		};
		setOnSeekBarChangeListener(sc);
	}
	
	public final void setRange(int max)
	{
		setRange(0, max, 1);
	}
	
	public final void setRange(int min, int max)
	{
		setRange(min, max, 1);
	}
	
	public final void setRange(int min, int max, int mul)
	{
		__min = min;
		__mul = mul;
		setMax(max - min);
	}
	
	public final void setValue(int value)
	{
		setProgress(value - __min);
	}
	
	public final void setOnValueListener(OnValueListener l)
	{
		__l = l;
	}
	
	public interface OnValueListener
	{
		void onValueChanged(mySeekBar s, int value, int mulValue);
		void onValueHolded(mySeekBar s, int value, int mulValue);
	}
	
}
