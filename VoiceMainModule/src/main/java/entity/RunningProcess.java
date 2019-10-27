package entity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;

import java.util.List;
import java.util.Vector;
/**
 * 获得移动终端所有应用进程的进程名
 *
 * @author Administrator
 */
public class RunningProcess
{

    private static Vector<String> runningProcessList = new Vector<String>();
    /**
     * 取得手机所有运行中的进程的进程名字，其中进程名字代表的是应用程序
     * 所在的包名。
     */
    public static Vector<String> getRunningProcessNames(Context context)
    {
        if (runningProcessList.size() > 0)
            return runningProcessList;
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> processInfos = manager
                .getRunningAppProcesses();
        for (RunningAppProcessInfo runningProcess : processInfos)
        {
            runningProcessList.add(runningProcess.processName);
        }
        return runningProcessList;
    }
}
