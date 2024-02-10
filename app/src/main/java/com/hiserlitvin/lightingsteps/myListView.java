package com.hiserlitvin.lightingsteps;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class myListView extends ListView
{
	private OnValueListener __l;
	
	public myListView(Context context)
	{
		super(context);
		__init();
	}
	public myListView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		__init();
	}
	public myListView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		__init();
	}
	
	private final void __init()
	{
		AdapterView.OnItemClickListener ic = new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id)
			{
				myListView lv = (myListView) parent;
				lv.setSelection(position);
				if (__l != null) __l.onValueChanged(lv, position);
			}
		};
		setOnItemClickListener(ic);
	}
	
	public final void setValue(int value)
	{
		setItemChecked(value, true);
		setSelection(value);
	}
	
	public final void setList(Context ctx, int strArray)
	{
		ListAdapter la = new ArrayAdapter<String>(ctx, R.layout.param_list, ctx.getResources().getStringArray(strArray));
		setAdapter(la);
	}
	
	public final void setOnValueListener(OnValueListener l)
	{
		__l = l;
	}
	
	public interface OnValueListener
	{
		void onValueChanged(myListView s, int value);
	}
}
