package com.hiserlitvin.lightingsteps;

import android.content.Context;

import androidx.fragment.app.Fragment;

public abstract class myFragment extends Fragment
{
	protected Context context;
	
	public myFragment(Context context)
	{
		super();
		this.context = context;
	}
	
	public abstract CharSequence getTitle();
	
}
