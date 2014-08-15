package com.example.logserver;

import java.net.*;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class HTTPService extends Service {

  private static final String TAG = HTTPService.class.getSimpleName();

  int port = PORT;
  Thread serverThread;
  private ServerSocket serverSocket;
  private boolean running = false;
  private String ip="0.0.0.0";
  
  public static final int PORT = 1234;

  private Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      if (msg.what == 1) {

      }
    }
  };
  
  private class LogcatOutput extends LogcatThread {

    public LogcatOutput(Socket socket, String[] cmds) {
      super(socket, cmds);
    }

    @Override
    public void onError(String msg) {
      dismissNotification(HTTPService.this.getApplicationContext());
      stopHttpServer();
    }

    @Override
    public void onSuccess(String msg) {
      showNotification(HTTPService.this.getApplicationContext(), msg);
      running = true;
    }

  }
  
  @Override
  public void onCreate() {
    super.onCreate();
    Log.d(TAG, "onCreate");
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    super.onStartCommand(intent, flags, startId);

    Log.d(TAG, "onStartCommand");

    if (intent != null
        && "com.example.logserver.intent.action.Update_Server".equalsIgnoreCase(intent.getAction())) {
      stopHttpServer();
    }

    if (!running) {
      startHttpServer();
    }

    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    Log.d(TAG, "onDestroy");
  }

  public void startHttpServer() {
    // Server runnable thread
    Runnable serverRunnable = new Runnable() {
      public void run() {
        try {
          InetAddress address = InetAddress.getByName(ip);
          serverSocket = new ServerSocket(port, 0, address);
          Log.d(TAG, "Socket = [" + serverSocket.getLocalSocketAddress().toString() + ":"
              + serverSocket.getLocalPort() + "]");
          while (!serverSocket.isClosed() && serverSocket.isBound()) {
            Socket socket = serverSocket.accept();
            socket.setTcpNoDelay(true);
            Log.d(TAG, "New Client: " + socket.getInetAddress().getHostAddress().toString());
            new LogcatOutput(socket, null).start();
          }

        } catch (Exception e) {
          // Error occured
          Log.d(TAG, "Error: " + e.getMessage());
        }
      }
    };

    // Run server-side processing on a seperate thread
    serverThread = new Thread(serverRunnable);
    serverThread.start();
  }

  public void stopHttpServer() {
    try {
      serverSocket.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    running = false;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
  
  private void showNotification(Context context, String text) {

    final NotificationManager nm =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    final Notification notifyDetails =
        new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());

    Intent notifyIntent = new Intent(context, MainActivity.class);
    notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    PendingIntent intent = PendingIntent.getActivity(context, 0, notifyIntent, 0);
    notifyDetails.setLatestEventInfo(context, context.getResources().getString(R.string.app_name),
        text, intent);
    nm.notify(1, notifyDetails);
  }
  
  private void dismissNotification(Context context) {

    final NotificationManager nm =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    nm.cancel(1);
  }
}
