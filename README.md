# android-wifi-connecter
Automatically exported from code.google.com/p/android-wifi-connecter

哈哈，打开google，一输入ScheduledExecutorService马上就显示 “ScheduledExecutorService 停止”。看来大家都不怎么知道该怎么停止这玩意儿啊。</br>

用法：</br>
ScheduledExecutorService exec =   Executors.newScheduledThreadPool(1); 
        exec.scheduleWithFixedDelay(new Runnable(){}, 0, 5, TimeUnit.SECOND);
