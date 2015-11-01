package com.farproc.wifi.ui;

import java.io.IOException;
import java.util.List;

import com.farproc.wifi.connecter.R;
import com.farproc.wifi.utils.ClientThread;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

// PreferenceActivity继承自ListActivity，哈哈，这我就放心了，可以放开使用ListActivity的方法了
public class WifiScanActivity extends PreferenceActivity {

	public static String TAG = "WifiScanActivity";
	private WifiManager mWifiManager;
	// 用一个List来保存扫描到的各个热点的扫描结果
	private List<ScanResult> mList_Results;

	private ListView mListView;
	private WifiapAdapter mAdapter;
	
	//这个主线程中的Handler负责处理ClientThread中的匿名子线程中发送过来的消息(当然内容是来自服务器端)
	private Handler mHandler;
	//与服务器通信的子线程
	private ClientThread mClientThread;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		
		initAdapter();

		initListView();
		
		initHandler();
		
		startNewThread();
		
		sendToServer();
	}

	private void sendToServer() {
		//TODO
		//定时向指定服务器发送热点信息
		if (isOnline() ) {
			Log.v(TAG,"网络连接畅通");
			
			if (mList_Results != null) {
				Log.v(TAG,"mList_Results不为null");
				for (int i = 0; i < mList_Results.size(); i++) {
					Message msg = new Message();
					msg.what = 0x111;
					msg.obj = mList_Results.get(i);
					if (mClientThread.rcvHandler != null) {
						mClientThread.rcvHandler.sendMessage(msg);
						Log.v(TAG, "clientThread.rcvHandler已发送消息：0x111");
					}
				}
				//这里将ClientThread的os给这个Activity调用，不知道是不是不太好
				//主要是因为要在for这个循环结束之后再关闭，如果不这么写，可以怎么做呢？
				//另外上面的rcvHandler是ClientThread的，也在这里调用了。。。
				try {
					mClientThread.os.close();
					Log.v(TAG, "ClientThread已向Socket中发送消息：0x111");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void startNewThread() {
		//加一个新线程用于与服务器通信
		mClientThread = new ClientThread(WifiScanActivity.this, mHandler);
		//在主线程中启动ClientThread线程用来与服务器通信
		new Thread(mClientThread).start();
	}

	private void initAdapter() {
		//这句话不能再onCreate()方法之前调用，即不能放在onCreate()方法的外面，因为系统得首先执行onCreate()
		mAdapter = new WifiapAdapter(this,mList_Results);
		// 为这个ListActivity设置Adapter
		setListAdapter(mAdapter);
	}

	private void initListView() {
		// 对ListView设置监听器
		mListView = getListView();
		mListView.setOnItemClickListener(mItemOnClick);
	}

	private void initHandler() {
		mHandler = new Handler(){
			@Override
			public void handleMessage (Message msg){
				//如果消息来自于子线程
				if(msg.what == 0x222){
					//先通知用户，已经接受到来自服务器的消息了
					Toast.makeText(WifiScanActivity.this, "已更新热点信息",Toast.LENGTH_SHORT).show();

					//TODO 还有后续的功能待完善。。。先不把msg显示出来
					Log.v("WifiScanActivity",msg.obj.toString());
				}
			}
		};
	}

	/**
	 * 每次Activity从启动到运行都会“注册”那个接收“扫描结果已经可用了”的Receiver，并且开始扫描
	 * 另外每次继续的时候就“注册”那个接收“扫描结果已经可用了”的Receiver，并且开始扫描
	 */
	@Override
	public void onResume() {
		super.onResume();
		final IntentFilter filter = new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		registerReceiver(mReceiver, filter);
		mWifiManager.startScan();
	}

	/**
	 * 每次暂停的时候就“注销”那个Receiver
	 */
	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
	}

/**
 * 判断Wifi是否处于连接状态
 */
	private boolean isWifiConnected(){
		return  ((ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
	}
	
	/**
	 * 判断网络连接是否畅通
	 */
	private boolean isOnline(){
	    ConnectivityManager connMgr =(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
	    //networkInfo不为空，且isConnected()返回true
	    return(networkInfo !=null&& networkInfo.isConnected());
	}
/**
 * 	获取BSSID
 */
	private String getBSSID(){
		return mWifiManager.getConnectionInfo().getBSSID();
	}
	// 这个Receiver是接收这个 “SCAN_RESULTS_AVAILABLE_ACTION”Action的
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			// An access point scan has completed
			if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
				mList_Results = mWifiManager.getScanResults();
				
				
				mAdapter.notifyDataSetChanged();

				mWifiManager.startScan();
			}

		}
	};
	
	public class WifiapAdapter extends BaseAdapter {

		private boolean isWifiConnected;
		private LayoutInflater inflater;
		private Context mmContext;

		public WifiapAdapter(Context context, List<ScanResult> list) {
			super();
			mmContext = context;
			mList_Results = list;
			inflater = getLayoutInflater();
			isWifiConnected = false;

		}
		
		@Override
		public int getCount() {
			if (mList_Results == null) {
				return 0;
			}
			return mList_Results.size();
		}

		@Override
		public Object getItem(int position) {
			return mList_Results.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ScanResult ap = mList_Results.get(position);
			ViewHolder viewHolder = null;
			isWifiConnected = false;
			
			if(convertView == null){
				viewHolder = new ViewHolder();
				convertView = inflater.inflate(R.layout.listitem_wifiap, null);
				viewHolder.iv_rssi = ((ImageView) convertView.findViewById(R.id.wifiap_item_iv_rssi));
	            viewHolder.tv_ssid = ((TextView) convertView.findViewById(R.id.wifiap_item_tv_ssid));
	            viewHolder.tv_desc = ((TextView) convertView.findViewById(R.id.wifiap_item_tv_desc));
				
				convertView.setTag(viewHolder);
			}
			else{
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			if (isWifiConnected() && ap.BSSID.equals(getBSSID())) {
	            isWifiConnected = true;
	        }

	        viewHolder.tv_ssid.setText(ap.SSID);
	        viewHolder.tv_desc.setText(getDesc(ap));
	        Picasso.with(mmContext).load(getRssiImgId(ap)).into(viewHolder.iv_rssi);
	        return convertView;
		}

		/**
		 * 根据具体的信号强度获取图标资源文件
		 * 
		 * @param ap
		 * @return
		 */
		private int getRssiImgId(ScanResult ap) {
			int imgId;
			// 若以连接，则直接将“Connected”的图标显示出来
			if (isWifiConnected) {
				imgId = R.drawable.ic_connected;
			} else {
				// 取信号强度（dbm）的绝对值
				int rssi = Math.abs(ap.level);
				if (rssi > 100) {
					imgId = R.drawable.ic_small_wifi_rssi_0;
				} else if (rssi > 80) {
					imgId = R.drawable.ic_small_wifi_rssi_1;
				} else if (rssi > 70) {
					imgId = R.drawable.ic_small_wifi_rssi_2;
				} else if (rssi > 60) {
					imgId = R.drawable.ic_small_wifi_rssi_3;
				} else {
					imgId = R.drawable.ic_small_wifi_rssi_4;
				}
			}
			return imgId;
		}
	
		private String getDesc(ScanResult ap) {
	        String desc = "";
	        
			String descOri = ap.capabilities;
			if (descOri.toUpperCase().contains("WPA-PSK")
					|| descOri.toUpperCase().contains("WPA2-PSK")) {
				desc = "Secured";
			} else {
				desc = "Open";
			}

	        // 是否连接此热点
	        if (isWifiConnected) {
	            desc = "Connected";
	        }
	        return desc;
	    }
	}

	// 装ListView中的每一项的容器
	public static class ViewHolder {
		public ImageView iv_rssi;
		public TextView tv_ssid;
		public TextView tv_desc;
	}
	// 每一项的“单击”监听器
	private OnItemClickListener mItemOnClick = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

			// 哦，其实开始就获取了 list_ScanResults这个List，而在这里把具体的ScanResult找出来
			final ScanResult result = mList_Results.get(position);
			launchWifiConnecter(WifiScanActivity.this, result);
		}
	};

	/**
	 * Try to launch Wifi Connecter with {@link #hostspot}. Prompt user to
	 * download if Wifi Connecter is not installed.
	 * 
	 * @param activity
	 * @param scanResult_hotspot
	 */
	private void launchWifiConnecter(final Activity activity,
			final ScanResult scanResult_hotspot) {
		// 话说其实只有MainActivity注册了这个Action。。。
		// 于是，其实下面的startActivity其实就是打开MainActivity（于是这个HOTSPOT的ScanResult对象就传到MainActivity中去了）
		final Intent intent = new Intent(WifiScanActivity.this,
				FloatingActivity.class);
		intent.putExtra("com.farproc.wifi.connecter.extra.HOTSPOT",
				scanResult_hotspot);
		activity.startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		
		getMenuInflater().inflate(R.menu.activity_main, menu); 
		return true;
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem mi){
		
		switch ( mi.getItemId() ){
		
		case R.id.action_send_to_server:
			if (isOnline() ) {
				Log.v(TAG,"网络连接畅通");
				sendToServer();
				
			}
			break;
		
		case R.id.action_stop_updating :
			if (isOnline() ) {
				Log.v(TAG,"网络连接畅通");
				
				//TODO 停止更新
				
			}
			break;
			
		case R.id.action_set_server:
			if(isOnline()) {
				Log.v(TAG,"网络连接畅通");
				
				//TODO 设置服务器IP和端口
				
			}
			break;
		}
		return true;
		
	}

}
