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

public class fragmentNoGranted extends Fragment
{
	private Context context;
	private View view;
	private OnClickListener click;
	
	public fragmentNoGranted(Context context)
	{
		this.context = context;
	}
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		view = inflater.inflate(R.layout.frm_device_nogranted, container, false);
		((Button)view.findViewById(R.id.grant_query)).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if (click != null) click.onGrantQuery();
			}
		});
		return view;
	}
	
	public final void setOnClickListener(OnClickListener click)
	{
		this.click = click;
	}
	
	public interface OnClickListener
	{
		void onGrantQuery();
	}
}
