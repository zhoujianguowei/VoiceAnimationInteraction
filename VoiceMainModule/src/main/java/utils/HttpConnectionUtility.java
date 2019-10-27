package utils;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * Created by Administrator on 2015/5/11.
 */
public class HttpConnectionUtility
{

    public static final String GET_REQUEST = "get_request";
    public static final String POST_REQUEST = "post_request";
    public static final int CONNECTION_TIME_OUT = 20000;
    public static final int READ_TIME_OUT = 20000;
    public static final int REQUEST_FAIL = -1;
    public static final int REQUEST_SUCCESS = 1;
    private Handler handler;
    private String url;
    private String methodType = GET_REQUEST;
    private Context context;
    private boolean showProgressDialog;
    private static ProgressDialog progressDialog;
    private HttpURLConnection urlConnection = null;
    /**
     * 新建线程进行网络请求,默认为true
     */
    private boolean inNewThread = true;
    public boolean isInNewThread()
    {
        return inNewThread;
    }
    public void setInNewThread(boolean inNewThread)
    {
        this.inNewThread = inNewThread;
    }
    /**
     * 请求头参数
     */
    private HashMap<String, Object> requestProperty;
    private Message msg;
    private ExecutorService executorService = null;
    public String getMessage()
    {
        return message;
    }
    public void setMessage(String message)
    {
        this.message = message;
    }
    private String message = "loading...";
    public String getMethodType()
    {
        return methodType;
    }
    public void setMethodType(String methodType)
    {
        this.methodType = methodType;
    }
    public Context getContext()
    {
        return context;
    }
    public void setContext(Context context)
    {
        this.context = context;
    }
    public boolean isShowProgressDialog()
    {
        return showProgressDialog;
    }
    public void setShowProgressDialog(boolean showProgressDialog)
    {
        this.showProgressDialog = showProgressDialog;
    }
    public HashMap<String, Object> getRequestProperty()
    {
        return requestProperty;
    }
    public void setRequestProperty(HashMap<String, Object> requestProperty)
    {
        this.requestProperty = requestProperty;
    }
    public HashMap<String, Object> getRequestBody()
    {
        return requestBody;
    }
    public void setRequestBody(HashMap<String, Object> requestBody)
    {
        this.requestBody = requestBody;
    }
    private HashMap<String, Object> requestBody;
    public HttpConnectionUtility(String url, Handler handler)
    {
        this.url = url;
        this.handler = handler;
    }
    public HttpConnectionUtility(String url, Handler handler,
                                 HashMap<String, Object> requestProperty,
                                 HashMap<String, Object> requestBody)
    {
        this(url, handler, requestProperty);
        this.requestBody = requestBody;
    }
    public HttpConnectionUtility(String url, Handler handler,
                                 HashMap<String, Object> requestProperty)
    {
        this(url, handler, POST_REQUEST);
        this.requestProperty = requestProperty;
    }
    public HttpConnectionUtility(String url, Handler handler, String methodType)
    {
        this(url, handler);
        if (methodType != GET_REQUEST && methodType != POST_REQUEST)
        {
            throw new IllegalArgumentException("没有该请求");
        }
        this.methodType = methodType;
    }
    public HttpConnectionUtility(String url, Handler handler, Context context)
    {
        this(url, handler, GET_REQUEST);
        this.context = context;
    }
    public HttpConnectionUtility(String url, Handler handler, String methodType,
                                 Context context, boolean showProgressDialog)
    {
        this(url, handler, context);
        this.methodType = methodType;
        this.showProgressDialog = showProgressDialog;
    }
    private void showProgressDialog()
    {
        if (context == null)
        {
            return;
        }
        if (context instanceof Activity)
        {
            ((Activity) context).runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    if (progressDialog == null)
                    {
                        progressDialog = new ProgressDialog(context);
                    }
                    progressDialog.setMessage(message);
                    progressDialog.show();
                }
            });
        }
    }
    private void dismissProgressDialog()
    {
        if (context == null)
        {
            return;
        }
        if (context instanceof Activity)
        {
            ((Activity) context).runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    if (progressDialog != null && progressDialog.isShowing())
                    {
                        progressDialog.dismiss();
                    }
                }
            });
        }
    }
    /**
     * 默认访问网络资源时候，建立新线程
     */
    public void executeRequest()
    {
        msg = Message.obtain();
        Log.e("url", url);
        msg.what = REQUEST_FAIL;
        try
        {
            urlConnection = getUrlConnection(url, methodType);
            if (requestProperty != null)
            {
                Set<String> keySets = requestProperty.keySet();
                Iterator<String> keyIterator = keySets.iterator();
                while (keyIterator.hasNext())
                {
                    String key = keyIterator.next();
                    urlConnection.setRequestProperty(key, requestProperty.get(key)
                            .toString());
                }
            }
            if (showProgressDialog)
            {
                showProgressDialog();
            }
            if (inNewThread)
            {
                executorService = Executors.newSingleThreadExecutor();
                executorService.execute(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        runExecute();
                    }
                });
            }
            else
            {
                runExecute();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    private void runExecute()
    {
        try
        {
            urlConnection.connect();
            if (requestBody != null)
            {
                Set<String> keySets = requestBody.keySet();
                Iterator<String> keyIterator = keySets.iterator();
                OutputStream writer = urlConnection.getOutputStream();
                while (keyIterator.hasNext())
                {
                    String key = keyIterator.next();
                    writer.write(requestBody.get(key).toString().getBytes("UTF-8"));
                }
            }
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK)
            {
                msg.what = REQUEST_SUCCESS;
                msg.obj = parserNetStream(urlConnection.getInputStream());
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                handler.sendMessage(msg);
                dismissProgressDialog();
                urlConnection.disconnect();
                if (executorService != null)
                {
                    executorService.shutdown();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    private static HttpURLConnection getUrlConnection(String url, String methodType)
    {
        HttpURLConnection urlConnection = null;
        try
        {
            urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setDoInput(true);
            if (methodType == GET_REQUEST)
            {
                urlConnection.setRequestMethod("GET");
            }
            else
            {
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setUseCaches(false);
            }
            urlConnection.setConnectTimeout(CONNECTION_TIME_OUT);
            urlConnection.setReadTimeout(READ_TIME_OUT);
            // 不许重定向
            // urlConnection.setInstanceFollowRedirects(false);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return urlConnection;
    }
    /**
     * 只是封装了http请求，没有重新建立线程发出请求
     */
    public HttpConnectionUtility(String url)
    {
        this.url = url;
    }
    /**
     * 不重新建立线程发送请求，只是封装http请求 返回的键值有两个，分别为status,result
     */
    public synchronized HashMap<String, Object> executeSingleAttachThread()
    {
        urlConnection = getUrlConnection(url, methodType);
        HashMap<String, Object> resMap = new HashMap<>();
        resMap.put("status", "fail");
        if (requestProperty != null)
        {
            Set<String> keySets = requestProperty.keySet();
            Iterator<String> keyIterator = keySets.iterator();
            while (keyIterator.hasNext())
            {
                String key = keyIterator.next();
                urlConnection.setRequestProperty(key, requestProperty.get(key)
                        .toString());
            }
        }
        try
        {
            urlConnection.connect();
            if (requestBody != null)
            {
                Set<String> keySets = requestBody.keySet();
                Iterator<String> keyIterator = keySets.iterator();
                OutputStream writer = urlConnection.getOutputStream();
                StringBuilder builder = new StringBuilder();
                while (keyIterator.hasNext())
                {
                    String key = keyIterator.next();
                    Object value = requestBody.get(key);
                    builder.append(URLEncoder.encode(key, "utf-8") + "=" +
                            URLEncoder.encode(value.toString(), "utf-8"));
                    builder.append("&");
                }
                if (builder.toString().endsWith("&"))
                    builder.deleteCharAt(builder.length() - 1); //remove last &
                writer.write(builder.toString().getBytes("utf-8"));
            }
            int responseCode = urlConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK)
            {
                resMap.put("status", "success");
                resMap.put("result",
                        parserNetStream(urlConnection.getInputStream()));
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                dismissProgressDialog();
                urlConnection.disconnect();
                if (executorService != null)
                {
                    executorService.shutdown();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return resMap;
    }
    public byte[] parserNetStream(InputStream inputstream)
    {
        ByteArrayOutputStream byteArrayOutputStream = null;
        byte[] readBytes = null;
        try
        {
            byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] perReadBytes = new byte[1024];
            int readLength = -1;
            while ((readLength = inputstream.read(perReadBytes)) != -1)
            {
                byteArrayOutputStream.write(perReadBytes, 0, readLength);
            }
            inputstream.close();
            readBytes = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return readBytes;
    }
}
