package com.hzc.easy.wifi.ap;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

public class HzcWifiAPService {
    private static final String TAG = "HzcWifiAPService";
    private OnScanResultListence onScanResultListence;
    private OnWifiStatusListence onWifiStatusListence;
    private Context mContext;
    private WifiManager mWifiManager;
    //监听wifi热点的状态变化
    private static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
    private static final String EXTRA_WIFI_AP_STATE = "wifi_state";
    private static final int WIFI_AP_STATE_DISABLED = 11;
    private static final int WIFI_AP_STATE_ENABLED = 13;
    private static final int WIFI_AP_STATE_FAILED = 14;

    public enum WifiSecurityType {
        WIFICIPHER_NOPASS, WIFICIPHER_WPA, WIFICIPHER_INVALID, WIFICIPHER_WPA2
    }

    public HzcWifiAPService(Context context) {
        Log.i(TAG, "WifiAPUtils construct");
        mContext = context;
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }


    /**
     * 监听wifi广播事件
     */
    private BroadcastReceiver mWifiStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "WifiAPUtils onReceive: " + intent.getAction());
            if (onWifiStatusListence == null) {
                Log.i(TAG, "not init Interface OnWifiListance");
                return;
            }
            if (WIFI_AP_STATE_CHANGED_ACTION.equals(intent.getAction())) {//当前的WiFi热点启用状态
                int cstate = intent.getIntExtra(EXTRA_WIFI_AP_STATE, -1);
                if (cstate == WIFI_AP_STATE_ENABLED) {
                    onWifiStatusListence.onApEnable();
                }
                if (cstate == WIFI_AP_STATE_DISABLED || cstate == WIFI_AP_STATE_FAILED) {
                    onWifiStatusListence.onApDisable();
                }
            } else if (intent.getAction().equalsIgnoreCase(mWifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {//获得热点搜索结果
                List<ScanResult> result = mWifiManager.getScanResults();
                if (result != null && result.size() > 0) {
                    Log.i(TAG, String.format("GET:%d wifi info", result.size()));
                    onScanResultListence.onFond(result);
                }
            } else if (intent.getAction().equalsIgnoreCase(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {//已链接一个热点
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                Log.i(TAG, "NetworkInfo = " + info.getDetailedState());
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                    if (wifiInfo.getBSSID() != null && !wifiInfo.getBSSID().isEmpty()) {
                        onWifiStatusListence.onConnectioned(wifiInfo);
                    }
                }
            }
        }
    };

    /**
     * 开启wifi搜索
     */
    public void registerListance() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WIFI_AP_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mContext.registerReceiver(mWifiStateBroadcastReceiver, filter);
    }

    /**
     * 取消注册
     */
    public void unRegisterListance() {
        mContext.unregisterReceiver(mWifiStateBroadcastReceiver);
    }

    public void startScan() {
        mWifiManager.startScan();
    }

    /**
     * wifi状态监听
     */
    public interface OnWifiStatusListence {
        void onApEnable();

        void onApDisable();

        void onConnectioned(WifiInfo wifiInfo);
    }

    public interface OnScanResultListence {
        void onFond(List<ScanResult> devicelist);
    }

    public void setOnWifiStatusListence(OnWifiStatusListence onWifiStatusListence) {
        this.onWifiStatusListence = onWifiStatusListence;
    }

    public void setOnScanResultListence(OnScanResultListence onScanResultListence) {
        this.onScanResultListence = onScanResultListence;
    }
}