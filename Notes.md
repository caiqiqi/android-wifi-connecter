# Bugs and solutions invloved

Wifi -> connectToNewNetwork() </br>
    -> connectToConfiguredNetwork() </br>
    -> ReenableAllApsWhenNetworkStateChanged.schedule(context) </br>
    -> context.startService(new Intent(context, BackgroundService.class)) </br>
    -> 
  `
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
      return null;
    }
    
    public void onCreate(){
      ...
      registerReceiver(mReceiver, mIntentFilter);
    }
    
    public void onDestroy(){
      inregisterReceiver(mReceiver);
    }
}
`
