package com.hiserlitvin.lightingsteps;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class pageDevice extends myFragment
{
	private View view;
	private onListener __l;
	
	public pageDevice(Context context)
	{
		super(context);
	}
	
	public CharSequence getTitle()
	{
		return context.getString(R.string.page_device);
	}
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.page_device, container, false);
		return view;
	}
	
	@Override
	public void onAttach(@NonNull Context context)
	{
		super.onAttach(context);
		if (__l != null) __l.onAttach();
	}
	
	public void setFragment(Fragment fragment)
	{
		/*if (isAdded())
		{*/
			FragmentManager fm = getChildFragmentManager();
			fm.beginTransaction()
					.replace(R.id.device_frame, fragment)
					.commit();
		//}
	}
	
	public void setOnListener(onListener listener)
	{
		__l = listener;
	}
	
	public interface onListener
	{
		void onAttach();
	}
}
