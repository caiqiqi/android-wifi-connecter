package com.farproc.wifi.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class ClientThread implements Runnable {
	
	public static String TAG = "ClientThread";
	
	private String SERVER_IP = Constants.SERVER_IP;
	private int SERVER_PORT = Constants.SERVER_PORT;
	
	private Context mContext;
	private Socket s;
	
	//向UI线程发送消息的Handler
	private Handler mHandler;
	//接收UI线程消息的Handler对象
	public Handler rcvHandler;
	
	//该线程所处理的Socket所对应的输入流
	private BufferedReader br;
	public OutputStream os;
	
	public ClientThread(Context context, Handler handler){
		this.mContext = context;
		this.mHandler = handler;
	}
	
	public void run(){

		Log.v(TAG,"ClientThread started");
		try {
			s = new Socket (SERVER_IP, SERVER_PORT);
			//判断Socket是否已连接到服务器
			if(s.isConnected()){
				Log.v("ClientThread", "Client connected to the server successfully!");
			}
			init();  
			
			//为当前线程初始化Looper
			Looper.prepare();
			
			//注意Handler要在onCreate中创建，而不是在Thread线程创建之后再创建
			//创建rcvHandler对象
			rcvHandler = new Handler(){
				@Override
				public void handleMessage(Message msg){
					
					if(msg.what == 0x111){
						
						try{
							
							if (s.isConnected()) {
								os.write((msg.obj.toString() + "\r\n").getBytes("utf-8"));
								//啊，怪不得服务器那边怎么老不关闭流，原来是这边客户端没有关闭，所以服务器那边一直在等
								//os.flush();
								//close()方法也调用了flush()，所以不用再调用了
								//有博主说如果你的文件读写没有达到预期目的，那么十有八九是因为没有调用flush()或者close()方法
								//os.close();
							} else {
								Toast.makeText(mContext, "Socket连接已断开，尝试重新连接...", Toast.LENGTH_SHORT).show();
								s = new Socket (SERVER_IP, SERVER_PORT);
								if (s.isConnected()){
									Toast.makeText(mContext, "Socket已连接！", Toast.LENGTH_SHORT).show();
								}
							}
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
							mHandler.sendMessage(msg);
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
			
		} catch (SocketTimeoutException e1){
			Log.v(TAG,"网络连接超时！");
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	private void init() throws IOException {
		br = new BufferedReader(new InputStreamReader(s.getInputStream()));
		os = s.getOutputStream();
	}
}