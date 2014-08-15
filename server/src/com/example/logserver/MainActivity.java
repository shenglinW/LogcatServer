package com.example.logserver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;

public class MainActivity extends Activity {

  public static final int DIALOG_LEVEL_FILTER_ID = 1;
  public static final int DIALOG_APP_FILTER_ID = 2;

  public static final int LEVEL_OPTION = Menu.FIRST;
  public static final int APP_OPTION = Menu.FIRST + 1;

  final CharSequence[] items = {"Debug", "Error", "Info", "Verbose", "Warn", "All"};
  final char[] mFilters = {'D', 'E', 'I', 'V', 'W'};

  private AlertDialog mDialog;
  private int mLevelFilter = -1;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    updateServerInfo();
    buttonInit();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    menu.add(Menu.NONE, LEVEL_OPTION, 1, "Level Filter").setIcon(android.R.drawable.ic_menu_manage);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case LEVEL_OPTION:
        onCreateDialog(DIALOG_LEVEL_FILTER_ID);
        break;
      case APP_OPTION:
        onCreateDialog(DIALOG_APP_FILTER_ID);
        break;
      default:
        break;
    }

    return false;
  }

  protected Dialog onCreateDialog(int id) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);

    switch (id) {
      case DIALOG_LEVEL_FILTER_ID:
        builder.setTitle("Select a filter level");
        builder.setSingleChoiceItems(items, mLevelFilter, mClickListener);
        mDialog = builder.create();
        break;
      case DIALOG_APP_FILTER_ID:
        builder.setTitle("Select a app to log");
        builder.setSingleChoiceItems(items, mLevelFilter, mClickListener);
        mDialog = builder.create();
        break;

      default:
        break;
    }

    mDialog.show();
    return mDialog;
  }

  DialogInterface.OnClickListener mClickListener = new DialogInterface.OnClickListener() {
    public void onClick(DialogInterface dialog, int which) {
      if (which == 5) {
        mLevelFilter = -1;
      } else {
        mLevelFilter = which;
      }

      updateFilter();
    }
  };

  private void updateFilter() {
    mDialog.dismiss();
  }

  private void buttonInit() {

    Button rebootButton = (Button) findViewById(R.id.rebootServerButton);
    rebootButton.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        rebootServer();
        Toast.makeText(MainActivity.this, "Server rebooted", Toast.LENGTH_SHORT).show();
      }

    });
  }

  private void rebootServer() {
    Intent it = new Intent("com.example.logserver.intent.action.Update_Server");
    if (mLevelFilter != -1) {
      StringBuilder sb = new StringBuilder();
      sb.append("*:");
      sb.append(mFilters[mLevelFilter]);
      it.putExtra("filter", sb.toString());
    }
    sendBroadcast(it);
    updateServerInfo();
  }

  private void updateServerInfo() {
    WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    TextView step3 = (TextView) findViewById(R.id.step3label);
    step3.setText("3. Access the URL: http://" + getSocketIP(wifiInfo) + ":" + HTTPService.PORT);
  }


  public void onDestroy() {
    super.onDestroy();
  }

  public String getSocketIP(WifiInfo wifiInfo) {
    int ipAddress = wifiInfo.getIpAddress();
    String ip = intToIp(ipAddress);
    return ip;
  }

  private String intToIp(int i) {
    return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "."
        + ((i >> 24) & 0xFF);
  }

}
