package com.farproc.wifi.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class ClientThread implements Runnable {
	
	public static String TAG = "ClientThread";
	
	private static String SERVER_IP = "192.168.24.1";
	private static int SERVER_PORT = 30000;
	private Socket s;
	
	//向UI线程发送消息的Handler
	private Handler handler;
	//接收UI线程消息的Handler对象
	public Handler rcvHandler;
	
	//该线程所处理的Socket所对应的输入流
	BufferedReader br;
	OutputStream os;
	
	public ClientThread(Handler handler){
		this.handler = handler;
		
	}
	
	public void run(){

		Log.v(TAG,"ClientThread started");
		try {
			s = new Socket (SERVER_IP, SERVER_PORT);
			if(s != null){
				Log.v("ClientThread", "Client connected to the server successfully!");
			}
			br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			os = s.getOutputStream();
			
			//为当前线程初始化Looper
			Looper.prepare();
			
			//创建rcvHandler对象
			rcvHandler = new Handler(){
				@Override
				public void handleMessage(Message msg){
					//接收到UI线程中用户输入的数据
					if(msg.what == 0x111){
						//将用户在文本框内输入的内容写到Socket中传输
						try{
							os.write((msg.obj.toString() + "\r\n").getBytes("utf-8"));
							Log.v(TAG,"ClientThread已向Socket中发送消息：0x111");
						} catch(Exception e){
							e.printStackTrace();
						}
					}
				}
			};
			//启动一条子线程来读取服务器响应的数据
			new Thread(){
				
				@Override
				public void run(){
					Log.v(TAG,"子线程开启");
					String content;
					//不断读取Socket输入流中的内容
					
					try {
						while((content = br.readLine()) != null){
							Log.v(TAG,"子线程读取到消息");
							//每当读取到来自服务器的数据(一行一行的)之后，
							//发送消息通知主线程，更新界面
							//显读到的数据
							Message msg = new Message();
							msg.what = 0x222;
							msg.obj = content;
							
							//***主线程中的Handler会处理的
							handler.sendMessage(msg);
							Log.v(TAG,"子线程的handler已发送消息：0x222");
						}
					} catch(IOException e){
						e.printStackTrace();
					}
				}
			}.start();
			
			//启动Looper
			//注意:写在Looper.loop()之后的代码不会被执行,这个函数内部应该是一个循环
			Looper.loop();
			
			
//			//创建rcvHandler对象
//			rcvHandler = new Handler(){
//				@Override
//				public void handleMessage(Message msg){
//					//接收到UI线程中用户输入的数据
//					if(msg.what == 0x111){
//						//将用户在文本框内输入的内容写到Socket中传输
//						try{
//							os.write((msg.obj.toString() + "\r\n").getBytes("utf-8"));
//							Log.v(TAG,"ClientThread已向Socket中发送消息：0x111");
//						} catch(Exception e){
//							e.printStackTrace();
//						}
//					}
//				}
//			};
			
		} catch (SocketTimeoutException e1){
			Log.v(TAG,"网络连接超时！");
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}