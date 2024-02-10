package com.hiserlitvin.lightingsteps;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

public class myFragmentPagerAdapter extends FragmentPagerAdapter
{
	private List<myFragment> fragments;
	
	public myFragmentPagerAdapter(List<myFragment> fragments, FragmentManager fm)
	{
		super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
		this.fragments = fragments;
	}
	
	@Nullable
	@Override
	public CharSequence getPageTitle(int position)
	{
		return fragments.get(position).getTitle();
	}
	
	@NonNull
	@Override
	public Fragment getItem(int position)
	{
		return fragments.get(position);
	}
	
	@Override
	public int getCount()
	{
		return fragments.size();
	}
}
