package com.bridgewalkerapp.androidclient;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragment;

public class TabListenerUtils<T extends SherlockFragment> implements ActionBar.TabListener {
	private final SherlockFragmentActivity hostActivity;
	private final String tag;
	private final Class<T> fragmentClass;
	private final Bundle args;
	private Fragment fragment;
	
	public TabListenerUtils(SherlockFragmentActivity hostActivity, String tag, Class<T> fragmentClass) {
		this(hostActivity, tag, fragmentClass, null);
	}
	
	public TabListenerUtils(SherlockFragmentActivity hostActivity, String tag, Class<T> fragmentClass, Bundle args) {
		this.hostActivity = hostActivity;
		this.tag = tag;
		this.fragmentClass = fragmentClass;
		this.args = args;
		
		FragmentManager fm = this.hostActivity.getSupportFragmentManager(); 
		this.fragment = fm.findFragmentByTag(this.tag);
		if (this.fragment != null && !this.fragment.isDetached()) {
			FragmentTransaction ft = fm.beginTransaction();
			ft.detach(this.fragment);
			ft.commit();
		}
	}
	
	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		if (this.fragment == null) {
			this.fragment = Fragment.instantiate(this.hostActivity,
										this.fragmentClass.getName(), this.args);
			ft.add(android.R.id.content, this.fragment, this.tag);
		} else {
			ft.attach(this.fragment);
		}
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		if (this.fragment != null) {
			ft.detach(this.fragment);
		}
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		/* nothing to do */
	}
}
