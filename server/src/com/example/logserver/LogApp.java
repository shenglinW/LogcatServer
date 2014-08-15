package com.example.logserver;

import android.app.Application;
import android.content.Intent;

public class LogApp extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    startService(new Intent(getApplicationContext(), HTTPService.class));
  }

}
