# android-wifi-connecter
Automatically exported from code.google.com/p/android-wifi-connecter

哈哈，打开google，一输入ScheduledExecutorService马上就显示 “ScheduledExecutorService 停止”。看来大家都不怎么知道该怎么停止这玩意儿啊。</br>

用法：</br>
`ScheduledExecutorService exec =   Executors.newScheduledThreadPool(1); 
        exec.scheduleWithFixedDelay(new Runnable(){}, 0, 5, TimeUnit.SECOND);` </br>

停止：</br>
`exec.shutdown();` </br>


关于 BufferedReader.readLine()方法 </br>
其返回值：
`A String containing the contents of the line, not including any line-termination characters, or null if the end of the stream has been reached`.
