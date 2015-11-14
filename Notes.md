# Bugs and solutions invloved

连接到某“新网络”的时候其实是先让它成为“已配置的网络”，然后再运行一个Service </br>
Wifi -> connectToNewNetwork() </br>
    -> connectToConfiguredNetwork() </br>
    -> ReenableAllApsWhenNetworkStateChanged.schedule(context) </br>
    -> context.startService(new Intent(context, BackgroundService.class)) </br>
    -> 
  ```
  ReenableAllApsWhenNetworkState$BackgroundService extends Service {
  
    private BroadcastReceiver mReceiver = new BroadcastReceiver(){
      public void onReceive(Context context, Intent intent){
      	if(!mReenabled) {
		mReenabled = true;
		reenableAllAps(context);
		//stop the service if it was previously started
		BackgroundService.this.stopSelf();
	  }
      
      }
        
    }
    
    public IBinder onBind(Intent intent){
      return null;// We need not bind to it at all.
    }
    
    public void onCreate(){
      ...
      registerReceiver(mReceiver, mIntentFilter);
    }
    
    public void onDestroy(){
      inregisterReceiver(mReceiver);
    }
}
```
