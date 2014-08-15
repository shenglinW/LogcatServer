package com.example.logserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.example.logserver.helper.LogcatHelper;


import android.text.TextUtils;
import android.util.Log;

public abstract class LogcatThread extends Thread {
    
  private static final String TAG = LogcatThread.class.getSimpleName();
  
  private static final String[] LOGCAT_CMD = new String[] {"logcat","-v","time",};
  private static final int BUFFER_SIZE = 8096;
  private Process mLogcatProc = null;
  private boolean mRunning = false;
  
  private Socket socket;
  private final String[] cmds;
  
  public LogcatThread(Socket socket, String[] cmds) {
    this.socket = socket;
    if (cmds == null) {
      this.cmds = LOGCAT_CMD;
    } else {
      this.cmds = cmds;
    }
  }

  public void run() {
    mRunning = true;

    Log.e(TAG, "run");
    
    try {
      mLogcatProc = LogcatHelper.getLogcatProcess(LogcatHelper.BUFFER_MAIN);
    } catch (IOException e) {
      Log.e(TAG, "run " + e.toString());
      return;
    }

    onSuccess("Logcat service is running.");
    
    BufferedReader reader = null;
    PrintWriter socketWriter = null;
    try {
      reader = new BufferedReader(new InputStreamReader(mLogcatProc.getInputStream()), BUFFER_SIZE);
      socketWriter = new PrintWriter(socket.getOutputStream(), true);
      
      String line;
      while (mRunning && (line = reader.readLine()) != null) {
        if (!mRunning) {
          break;
        }

        if (TextUtils.isEmpty(line)) {
          continue;
        }

        socketWriter.write(line);
        socketWriter.write("\r\n");
        socketWriter.flush();
      }
    } catch (Exception e) {
      Log.e(TAG, "run " + e.toString());
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {}
      }
      if (socketWriter != null) {
        socketWriter.close();
      }
      stopLogcat();
    }
    
    Log.e(TAG, "run out");
  }

  public void stopLogcat() {
    if (mLogcatProc == null) return;

    onError("Stop");
    
    mLogcatProc.destroy();
    mLogcatProc = null;
    mRunning = false;
    
    if (socket != null) {
      try {
        socket.shutdownOutput();
        socket.close();
      } catch (IOException e) {} finally {
        socket = null;
      }
    }
  }

  public boolean isRunning() {
    return mRunning;
  }
 
  public abstract void onError(String msg);
  public abstract void onSuccess(String msg);
}
