/**
 * fixed ListView in ScrollView
 * from https://askdev.ru/q/kak-ya-mogu-pomestit-listview-v-scrollview-bez-ego-svorachivaniya-3804/
 */

package com.tmd.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class fixScrollView extends ScrollView
{
	
	public fixScrollView(Context context) {
		super(context);
	}
	
	public fixScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public fixScrollView(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev)
	{
		final int action = ev.getAction();
		switch (action)
		{
			case MotionEvent.ACTION_DOWN:
				super.onTouchEvent(ev);
				break;
			
			case MotionEvent.ACTION_MOVE:
				return false;
			
			case MotionEvent.ACTION_CANCEL:
				super.onTouchEvent(ev);
				break;
			
			case MotionEvent.ACTION_UP:
				return false;
			
			default:
				break;
		}
		return false;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		super.onTouchEvent(ev);
		return true;
	}
}
