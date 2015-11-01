package com.farproc.wifi.utils;

import android.app.AlarmManager;
import android.content.Context;


public class AlarmManagerUtil {

	/**
	 * 获取Alarm服务
	 * @author caiqiqi
	 *
	 */
	public static AlarmManager getAlarmManager(Context ctx){
		return (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
	}
	
	/**
	 * 周期性地向指定服务器发送热点信息
	 */
	public static void sendToServerRepeat(Context ctc){
		//TODO 待完成
	}
	
	
}
