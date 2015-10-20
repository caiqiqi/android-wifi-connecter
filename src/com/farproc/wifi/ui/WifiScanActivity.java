package com.farproc.wifi.ui;

import java.util.List;

import com.farproc.wifi.connecter.R;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

// PreferenceActivity继承自ListActivity，哈哈，这我就放心了，可以放开使用ListActivity的方法了
public class WifiScanActivity extends PreferenceActivity {

	private WifiManager mWifiManager;
	// 用一个List来保存扫描到的各个热点的扫描结果
	private List<ScanResult> mList_Results;

	private ListView mListView;
	private WifiapAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		
		//这句话不能再onCreate()方法之前调用，即不能放在onCreate()方法的外面，因为系统得首先执行onCreate()
		mAdapter = new WifiapAdapter(this,mList_Results);
		// 为这个ListActivity设置Adapter
		setListAdapter(mAdapter);

		// 对ListView设置监听器
		mListView = getListView();
		mListView.setOnItemClickListener(mItemOnClick);
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

				// Notifies the attached observers that the underlying data has
				// been changed and any View reflecting the data set should
				// refresh itself.
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

}
