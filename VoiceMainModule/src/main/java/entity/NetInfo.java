package entity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
public class NetInfo
{

    static ConnectivityManager connectivityManager;
    public static boolean isNetAvailable(Context context)
    {
        if (connectivityManager == null)
            connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfos = connectivityManager.getAllNetworkInfo();
        if (networkInfos == null)
            return false;
        for (NetworkInfo networkInfo : networkInfos)
        {
            if (networkInfo.getState() == State.CONNECTED)
            {
                return true;
            }
        }
        return false;
    }
}
