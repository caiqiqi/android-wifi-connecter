/*
 * Wifi Connecter
 * 
 * Copyright (c) 2011 Kevin Yuan (farproc@gmail.com)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 **/ 

package com.farproc.wifi.connecter;

import com.farproc.wifi.connecter.R;
import com.farproc.wifi.ui.Floating;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public abstract class BaseContent implements Floating.Content, OnCheckedChangeListener {

	protected final WifiManager mWifiManager;
	protected final Floating mFloating;
	protected final ScanResult mScanResult;
	protected final String mScanResultSecurity;
	protected final boolean mIsOpenNetwork ;
	
	protected int mNumOpenNetworksKept;
	
	protected View mView;
	
	//“取消”按钮监听器
	protected OnClickListener mCancelOnClick = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			mFloating.finish();
		}
	};
	
	protected String getCancelString() {
		return mFloating.getString(R.string.cancel);
	}
//信号强度：Poor,Fair,Good,Excellent四个级别
	//private static final int[] SIGNAL_LEVEL = {R.string.wifi_signal_0, R.string.wifi_signal_1, R.string.wifi_signal_2, R.string.wifi_signal_3};
	
	@SuppressWarnings("deprecation")
	public BaseContent(final Floating floating, final WifiManager wifiManager, final ScanResult scanResult) {
		super();
		mWifiManager = wifiManager;
		mFloating = floating;
		mScanResult = scanResult;
		mScanResultSecurity = Wifi.ConfigSec.getScanResultSecurity(mScanResult);
		mIsOpenNetwork =  Wifi.ConfigSec.isOpenNetwork(mScanResultSecurity);
		
		mView = View.inflate(mFloating, R.layout.base_content, null);
		//信号强度：改成以dbm为单位
		((TextView)mView.findViewById(R.id.SignalStrength_TextView)).setText(mScanResult.level + " dbm");
		//频率：以MHz为单位
		((TextView)mView.findViewById(R.id.Frequency_TextView)).setText(mScanResult.frequency + " MHz");
		final String rawSecurity = Wifi.ConfigSec.getDisplaySecirityString(mScanResult);
		final String readableSecurity = Wifi.ConfigSec.isOpenNetwork(rawSecurity) ? mFloating.getString(R.string.wifi_security_open) : rawSecurity; 
		//安全性
		((TextView)mView.findViewById(R.id.Security_TextView)).setText(readableSecurity);
		//“是否显示密码”按钮
		((CheckBox)mView.findViewById(R.id.ShowPassword_CheckBox)).setOnCheckedChangeListener(this);
		
		mNumOpenNetworksKept =  Settings.Secure.getInt(floating.getContentResolver(),
	            Settings.Secure.WIFI_NUM_OPEN_NETWORKS_KEPT, 10);
	}
	
	
	@Override
	public View getView() {
		return mView;
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		((EditText)mView.findViewById(R.id.Password_EditText)).setInputType(
				InputType.TYPE_CLASS_TEXT |
				(isChecked ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
						:InputType.TYPE_TEXT_VARIATION_PASSWORD));		
	}
	
//“修改密码”按钮监听器
	public OnClickListener mChangePasswordOnClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			changePassword();
		}
		
	};
	
	public void changePassword() {
		mFloating.setContent(new ChangePasswordContent(mFloating, mWifiManager, mScanResult));
	}

}
