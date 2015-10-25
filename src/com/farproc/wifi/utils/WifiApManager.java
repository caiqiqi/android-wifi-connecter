package com.farproc.wifi.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class WifiApManager {

	private Context mContext;
//	private final WifiManager mWifiManager;
	
	public WifiApManager(Context context) {
		this.mContext = context;
	}
	
//	public void getClientList(final int reachableTimeout, final FinishScanListener finishListener) {
//
//		Runnable runnable = new Runnable() {
//			public void run() {
//
//				BufferedReader br = null;
//				final ArrayList<ClientScanResult> list_result = new ArrayList<ClientScanResult>();
//				
//				try {
//					br = new BufferedReader(new FileReader("/proc/net/arp"));
//					String line;
//					while ((line = br.readLine()) != null) {
//						String[] splitted = line.split(" +");
//						//split(" +") 按空格进行拆分（也就是说只有按空格键流出来的空白才会是拆分的一句）
//
//						if ((splitted != null) && (splitted.length >= 4)) {
//							// Basic sanity check(基本的可用性测试)，即至少满足if条件的才算是有内容
//									/*
//									 * 总共cat出来有6项：
//									 * IP,HW type,flags,HW address,Mask,Device
//									 */
//									list_result.add(new ClientScanResult(splitted[0], splitted[3], splitted[5]));
//						}
//					}
//				} catch (Exception e) {
//					Log.e(this.getClass().toString(), e.toString());
//				} finally {
//					try {
//						br.close();
//					} catch (IOException e) {
//						Log.e(this.getClass().toString(), e.getMessage());
//					}
//				}
//
//				// Get a handler that can be used to post to the main thread
//				Handler mainHandler = new Handler(mContext.getMainLooper());
//				//获取主线程的Looper，作为参数传给这里的Handler
//				Runnable myRunnable = new Runnable() {
//					@Override
//					public void run() {
//						finishListener.onFinishScan(list_result);
//					}
//				};
//				//把这个Runnable对象交出去，给Handler
//				mainHandler.post(myRunnable);
//			}
//		};
//
//		Thread mythread = new Thread(runnable);
//		mythread.start();
//	}

//	public void getClientList(final FinishScanListener finishListener){
//		getClientList(300,finishListener);
//	}
}
