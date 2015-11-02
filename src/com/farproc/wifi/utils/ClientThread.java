package com.farproc.wifi.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

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
	
	private OutputStream os;
	
//	//用PrintWriter作为输出流
//	public PrintWriter out;
	
	public ClientThread(Context context, Handler handler){
		this.mContext = context;
		this.mHandler = handler;
	}
	
	public void run(){

		Log.v(TAG,"ClientThread started");
		try {
			
			
			initSocket();
			//为当前线程初始化Looper
			Looper.prepare();
			
			//注意Handler要在onCreate中创建，而不是在Thread线程创建之后再创建
			//创建rcvHandler对象
			rcvHandler = new Handler(){
				@Override
				public void handleMessage(Message msg){
					
					if(msg.what == 0x111){
						
						List<ScanResult> result = (List<ScanResult>) msg.obj;
						
						try{
							
//							if(! s.isConnected()){
//								
//								initSocket();
//							}
							
							StringBuilder builder = new StringBuilder(); 
							for (ScanResult scanResult : result) {
								builder.append(scanResult.toString() + "\r\n");
							}
							
							try {
								os.write( (builder.toString() ).getBytes("utf-8"));
								os.flush();
//								os.close();
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
							
							} catch(Exception e){
							e.printStackTrace();
						}
					}
				}

				private void sendToServer(List<ScanResult> result) throws IOException, UnsupportedEncodingException {
					//执行到下面的时候是肯定有socket连接的
					List<ScanResult> list = (List<ScanResult>) result;
					for (ScanResult r : list) {
						os.write((r.toString() + "\r\n").getBytes("utf-8"));
						os.flush();
						//这里不用close()，直接flush()就可以搞定。另外如果这里close()了，OutputStream就不容易再打开了
					}
					os.close();
					
					if(s != null){
						try {
							//因为OutputStream已经关闭了，于是Socket不得不关掉，然后再开Socket
							s.close();
							initSocket();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					//os.close();
					// 啊，怪不得服务器那边怎么老不关闭流，原来是这边客户端没有关闭，所以服务器那边一直在等
					// os.flush();
					// close()方法也调用了flush()，所以不用再调用了
					// 有博主说如果你的文件读写没有达到预期目的，那么十有八九是因为没有调用flush()或者close()方法
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
			
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	private void initSocket() throws IOException {
		s = new Socket (SERVER_IP, SERVER_PORT);
		br = new BufferedReader(new InputStreamReader(this.s.getInputStream()));
		os = this.s.getOutputStream();
	}
	
//private void sendUsingThreadPool(final List<ScanResult> scanResult){
//		
//		//启动一个线程每10秒钟
//        mExecutor =   Executors.newScheduledThreadPool(1); 
//        mExecutor.scheduleWithFixedDelay(new Runnable(){
//
//			@Override
//			public void run() {
//				
//				//为了一次性发送（即将这个List作为一个整体，好让服务器端能马上判断这个List结束了）
//				StringBuilder builder = new StringBuilder(); 
//				List<ScanResult> list = scanResult;
//				for (ScanResult result : list) {
//					builder.append(result.toString() + "\r\n");
//				}
//				
//				try {
//					os.write( (builder.toString() ).getBytes("utf-8"));
//					os.flush();
//					os.close();
//				} catch (UnsupportedEncodingException e) {
//					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
//				} finally {
//					
//					if(s != null){
//						try {
//							s.close();
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//					}
//				}
//			}
//        	
//        }, 0, 10, TimeUnit.SECONDS);
//	}
}