package com.hzc.easy.wifi.ap;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class HzcWifiAPService {
    private final String TAG = "HzcWifiAPService";
    private final String DEFAULT_WIFI_PWD = "12345678";
    private Context mContext;
    private WifiManager mWifiManager;
    private final int WIFI_AP_STATE_DISABLED = 11;
    private final int WIFI_AP_STATE_ENABLED = 13;
    private final int WIFI_AP_STATE_FAILED = 14;
    private final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";

    //TODO test
    private ConnectivityManager cm;
    private Handler handler;

    public enum WifiSecurityType {
        WIFICIPHER_NOPASS, WIFICIPHER_WPA, WIFICIPHER_INVALID, WIFICIPHER_WPA2
    }

    public HzcWifiAPService(Context context) {
        mContext = context;
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        handler = new Handler();
    }


    /**
     * 监听wifi广播事件
     */
    private BroadcastReceiver mWifiStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "WifiAPUtils onReceive: " + intent.getAction());
            switch (intent.getAction()) {
                case WIFI_AP_STATE_CHANGED_ACTION: {//热点启动状态
                    int status = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                    if (onWifiApStatusListence != null) {
                        //已开启
                        if (status == WIFI_AP_STATE_ENABLED) {
                            onWifiApStatusListence.onOpened();
                        }
                        //已关闭
                        if (status == WIFI_AP_STATE_DISABLED) {
                            onWifiApStatusListence.onClosed();
                        }
                        //操作失败
                        if (status == WIFI_AP_STATE_FAILED) {
                            onWifiApStatusListence.error();
                        }
                    }
                }
                break;
                case WifiManager.WIFI_STATE_CHANGED_ACTION: {//当前的WiFi启用状态
                    int status = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                    if (onWifiStatusListence != null) {
                        switch (status) {
                            case WifiManager.WIFI_STATE_DISABLED: {//已关闭
                                onWifiStatusListence.onClosed();
                            }
                            break;
                            case WifiManager.WIFI_STATE_DISABLING: {//关闭中
                                onWifiStatusListence.onCloseing();
                            }
                            break;
                            case WifiManager.WIFI_STATE_ENABLED: {//已开启
                                onWifiStatusListence.onOpened();
                            }
                            break;
                            case WifiManager.WIFI_STATE_ENABLING: {//正在开启
                                onWifiStatusListence.onOpening();
                            }
                            break;
                            case WifiManager.WIFI_STATE_UNKNOWN: {//未知异常
                                onWifiStatusListence.onError();
                            }
                            break;
                        }
                    }
                }
                break;
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION: {//获得热点列表
                    if (onScanListence != null) {
                        List<ScanResult> result = mWifiManager.getScanResults();
                        if (result != null && result.size() > 0) {
                            Log.i(TAG, String.format("scan wifi ap success . fined %d ap", result.size()));
                        }
                        onScanListence.onFind(result);
                        onScanListence.onScanDone();
                    }
                }
                break;
                case WifiManager.NETWORK_STATE_CHANGED_ACTION: {//网络状态改变
                    NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    Log.i(TAG, "NetworkInfo = " + networkInfo.getDetailedState());
                    //扫描中
                    if (onScanListence != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.SCANNING) {
                        onScanListence.onScaning();
                    }
                    if (onConnectionStatusListence != null) {
                        //断开链接
                        if (networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
                            onConnectionStatusListence.onDisConnected(networkInfo);
                        }
                        //已链接
                        if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                            WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                            if (wifiInfo.getBSSID() != null && !wifiInfo.getBSSID().isEmpty()) {
                                onConnectionStatusListence.onConnectioned(wifiInfo, networkInfo);
                            }
                        }
                    }
                }
                break;
                case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION: {//网络握手状态
//                    SupplicantState a = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
//                    Object b = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
//                    Log.i(TAG, "WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.EXTRA_NEW_STATE = " + a);
//                    Log.i(TAG, "WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.EXTRA_SUPPLICANT_ERROR = " + b);
                }
                break;
                case ConnectivityManager.CONNECTIVITY_ACTION: {//wifi链接情况
//                    NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
//                    boolean bool = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
//                    Log.i(TAG, "ConnectivityManager.CONNECTIVITY_ACTION." + String.valueOf(bool) + "  " + (info != null ? info.toString() : "null"));
                }
                break;
            }
        }
    };

    /**
     * 注册广播服务
     */
    public void doRegisterListance() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);//WiFi模块硬件状态改变的广播
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);//扫描状态
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);//wifi链接发生改变
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);//网络是否链接成功/失败
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);//链接的热点正在发生一些变化
        filter.addAction(WifiManager.RSSI_CHANGED_ACTION);//热点强度变化
        filter.addAction(WIFI_AP_STATE_CHANGED_ACTION);//个人共享热点状态监听

        mContext.registerReceiver(mWifiStateBroadcastReceiver, filter);
    }

    /**
     * 取消注册
     */
    public void doUnRegisterListance() {
        mContext.unregisterReceiver(mWifiStateBroadcastReceiver);
    }

    /**
     * 启动热点扫描
     *
     * @return
     */
    public boolean doStartScan() {
        boolean status = mWifiManager.startScan();
        Log.i(TAG, "start scan was " + String.valueOf(status));
        return status;
    }

    /**
     * 关闭wifi热点
     */
    public void doCloseWifiAp() {
        if (isWifiApEnabled()) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    cm.getClass().getMethod("stopTethering",int.class).invoke(cm,0);
                    return;
                }
                Method method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
                method.setAccessible(true);
                WifiConfiguration config = (WifiConfiguration) method.invoke(mWifiManager);
                Method method2 = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                method2.invoke(mWifiManager, config, false);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                Log.e(TAG,e.toString());
            }
        }
    }

    /**
     * 热点是否打开
     *
     * @return
     */
    public boolean isWifiApEnabled() {
        try {
            Method method = mWifiManager.getClass().getMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(mWifiManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * wifi是否已打开
     *
     * @return
     */
    public boolean isWifiEnabled() {
        return mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED;
    }

    /**
     * 获得本地IP地址
     *
     * @return
     */
    private String getLocationIpAddress() {
        //获取wifi服务
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            int ipAddress = wifiInfo.getIpAddress();
            return calcIpAddress(ipAddress);
        }
        return "";
    }

    /**
     * 获得服务器wifi地址
     *
     * @return
     */
    private String getServerIpAddress() {
        //获取wifi服务
        DhcpInfo wifiInfo = mWifiManager.getDhcpInfo();
        if (wifiInfo != null) {
            int ipAddress = wifiInfo.serverAddress;
            return calcIpAddress(ipAddress);
        }
        return "";
    }

    /**
     * 计算ip地址
     *
     * @param i
     * @return
     */
    private String calcIpAddress(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    /**
     * 启动热点，默认wap2加密
     *
     * @param str
     * @param password
     * @return
     */
    public boolean doCreateWifiAp(String str, String password) {
        return doCreateWifiAp(str, password, HzcWifiAPService.WifiSecurityType.WIFICIPHER_WPA2);
    }


    /**
     * 创建启动WiFi热点
     *
     * @param str
     * @param password
     * @param Type
     * @return
     */
    public boolean doCreateWifiAp(String str, String password, HzcWifiAPService.WifiSecurityType Type) {
        String ssid = str;
        //配置热点信息。
        WifiConfiguration wcfg = new WifiConfiguration();
        wcfg.SSID = new String(ssid);
        wcfg.networkId = 5512311;
        wcfg.allowedAuthAlgorithms.clear();
        wcfg.allowedGroupCiphers.clear();
        wcfg.allowedKeyManagement.clear();
        wcfg.allowedPairwiseCiphers.clear();
        wcfg.allowedProtocols.clear();

        if (Type == HzcWifiAPService.WifiSecurityType.WIFICIPHER_NOPASS) {
            wcfg.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN, true);
            wcfg.wepKeys[0] = "";
            wcfg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wcfg.wepTxKeyIndex = 0;
        } else if (Type == HzcWifiAPService.WifiSecurityType.WIFICIPHER_WPA) {
            //密码至少8位，否则使用默认密码
            if (null != password && password.length() >= 8) {
                wcfg.preSharedKey = password;
            } else {
                wcfg.preSharedKey = DEFAULT_WIFI_PWD;
            }
            wcfg.hiddenSSID = false;
            wcfg.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wcfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wcfg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            //wcfg.allowedKeyManagement.set(4);
            wcfg.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wcfg.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wcfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wcfg.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        } else if (Type == HzcWifiAPService.WifiSecurityType.WIFICIPHER_WPA2) {
            Log.d(TAG, "wifi ap---- wpa2");
            //密码至少8位，否则使用默认密码
            if (null != password && password.length() >= 8) {
                wcfg.preSharedKey = password;
            } else {
                wcfg.preSharedKey = DEFAULT_WIFI_PWD;
            }
            wcfg.hiddenSSID = false;
            wcfg.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wcfg.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            wcfg.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            wcfg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wcfg.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wcfg.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wcfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wcfg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        }
        try {
            Method method = mWifiManager.getClass().getMethod("updateNetwork",
                    wcfg.getClass());
            Object rt = method.invoke(mWifiManager, wcfg);
            Log.d(TAG, " rt = " + rt);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return setWifiApEnabled();
    }

    /**
     * 启动，关闭wifi
     *
     * @param enable
     */
    public boolean setWifiEnable(boolean enable) {
        return mWifiManager.setWifiEnabled(enable);
    }

    /**
     * 启动热点
     *
     * @return
     */
    private boolean setWifiApEnabled() {
        //开启wifi热点需要关闭wifi
        while (mWifiManager.getWifiState() != WifiManager.WIFI_STATE_DISABLED) {
            setWifiEnable(false);
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                return false;
            }
        }
        // 确保wifi 热点关闭。
        while (getWifiAPState() != WIFI_AP_STATE_DISABLED) {
            try {
                doCloseWifiAp();
                Thread.sleep(100);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                return false;
            }
        }

        //开启wifi热点
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                Field field = cm.getClass().getDeclaredField("TETHERING_WIFI");
                field.setAccessible(true);
                int mTETHERING_WIFI = (int) field.get(cm);

                Field iConnMgrField = cm.getClass().getDeclaredField("mService");
                iConnMgrField.setAccessible(true);
                Object iConnMgr = iConnMgrField.get(cm);
                Class<?> iConnMgrClass = Class.forName(iConnMgr.getClass().getName());
                Method startTethering = iConnMgrClass.getMethod("startTethering", int.class, ResultReceiver.class, boolean.class);
                startTethering.invoke(iConnMgr, mTETHERING_WIFI, new ResultReceiver(handler) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        super.onReceiveResult(resultCode, resultData);
                    }
                }, true);
            } else {
                Method method1 = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                method1.invoke(mWifiManager, null, true);
            }
            Thread.sleep(200);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
        return true;
    }


    /**
     * 获取热点状态
     */
    private int getWifiAPState() {
        int state = -1;
        try {
            Method method2 = mWifiManager.getClass().getMethod("getWifiApState");
            state = (Integer) method2.invoke(mWifiManager);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        Log.i("WifiAP", "getWifiAPState.state " + state);
        return state;
    }

    /**
     * 是否已链接当前的wifi热点
     *
     * @param BSSID
     * @return
     */
    public boolean isConnection(String BSSID) {
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo != null && wifiInfo.getBSSID() != null)
            return wifiInfo.getBSSID().equalsIgnoreCase(BSSID);
        return false;
    }

    /**
     * 获取热点ssid
     *
     * @return
     */
    public String getApSsid() {
        try {
            Method method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
            WifiConfiguration configuration = (WifiConfiguration) method.invoke(mWifiManager);
            return configuration.SSID;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    /**
     * 获取热点密码
     *
     * @return
     */
    public String getApPassword() {
        try {
            Method method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
            WifiConfiguration configuration = (WifiConfiguration) method.invoke(mWifiManager);
            return configuration.preSharedKey;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return null;
        }

    }

    /**
     * 获取热点安全类型
     *
     * @return
     */
    public int getApSecurity() {
        WifiConfiguration configuration;
        try {
            Method method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
            configuration = (WifiConfiguration) method.invoke(mWifiManager);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return HzcWifiAPService.WifiSecurityType.WIFICIPHER_INVALID.ordinal();
        }

        Log.i(TAG, "getSecurity security=" + configuration.allowedKeyManagement);
        if (configuration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE)) {
            return HzcWifiAPService.WifiSecurityType.WIFICIPHER_NOPASS.ordinal();
        } else if (configuration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            return HzcWifiAPService.WifiSecurityType.WIFICIPHER_WPA.ordinal();
        } else if (configuration.allowedKeyManagement.get(4)) { //4 means WPA2_PSK
            return HzcWifiAPService.WifiSecurityType.WIFICIPHER_WPA2.ordinal();
        }
        return HzcWifiAPService.WifiSecurityType.WIFICIPHER_INVALID.ordinal();
    }

    /**
     * 已记录过的wifi
     *
     * @param SSID
     * @return
     */
    public WifiConfiguration isExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"") /*&& existingConfig.preSharedKey.equals("\"" + password + "\"")*/) {
                return existingConfig;
            }
        }
        return null;
    }

    /**
     * 链接一个wifi
     *
     * @param scanResult
     * @return
     */
    public boolean doConnection(ScanResult scanResult) {
        return doConnection(scanResult, DEFAULT_WIFI_PWD);
    }

    /**
     * 断开指定ID的网络
     *
     * @param SSID
     * @return
     */
    public boolean doDisconnectWifi(String SSID) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"") /*&& existingConfig.preSharedKey.equals("\"" + password + "\"")*/) {
                mWifiManager.disableNetwork(existingConfig.networkId);
                return mWifiManager.disconnect();
            }
        }
        return false;
    }

    /**
     * 链接一个wifi
     *
     * @param scanResult
     * @param pwd
     * @return
     */
    public boolean doConnection(ScanResult scanResult, String pwd) {
        try {
            int wifiid = doAddWifiConfig(scanResult, pwd);
            if (wifiid != -1) {
                return mWifiManager.enableNetwork(wifiid, true);
            }
        } catch (Exception e) {

        }
        return false;
    }

    /**
     * 添加wifi配置信息
     *
     * @param wifi
     * @param pwd
     * @return
     */
    private int doAddWifiConfig(ScanResult wifi, String pwd) {
        WifiConfiguration tempConfig = isExsits(wifi.SSID);
        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }
        int wifiid = -1;
        Log.d(TAG, "equals");
        WifiConfiguration wifiCong = new WifiConfiguration();
        wifiCong.SSID = "\"" + wifi.SSID + "\"";
        wifiCong.preSharedKey = "\"" + pwd + "\"";
        wifiCong.hiddenSSID = false;
        wifiCong.status = WifiConfiguration.Status.ENABLED;
        wifiid = mWifiManager.addNetwork(wifiCong);
        if (wifiid != -1) {
            return wifiid;
        }
        return wifiid;
    }

    /**
     * wifi物理状态监听
     * WifiManager.WIFI_STATE_CHANGED_ACTION
     * WifiManager.EXTRA_WIFI_STATE
     */
    public interface OnWifiStatusListence {
        void onOpened();

        void onOpening();

        void onClosed();

        void onCloseing();

        void onError();
    }

    /**
     * wifi热点物理状态监听
     * WifiManager.WIFI_STATE_CHANGED_ACTION
     * WifiManager.EXTRA_WIFI_STATE
     */
    public interface OnWifiApStatusListence {

        void onOpened();

        void onClosed();

        void error();

    }

    /**
     * 链接状态改变事件
     * ConnectivityManager.CONNECTIVITY_ACTION
     * ConnectivityManager.EXTRA_NO_CONNECTIVITY
     */
    public interface OnConnectionStatusListence {

        void onConnectioned(WifiInfo wifiInfo, NetworkInfo networkInfo);

        void onDisConnected(NetworkInfo networkInfo);

    }

    /**
     * 扫描wifi热点结果
     */
    public interface OnScanListence {

        void onScaning();

        void onFind(List<ScanResult> devicelist);

        void onScanDone();
    }

    private OnScanListence onScanListence;
    private OnConnectionStatusListence onConnectionStatusListence;
    private OnWifiApStatusListence onWifiApStatusListence;
    private OnWifiStatusListence onWifiStatusListence;

    public void setOnWifiApStatusListence(OnWifiApStatusListence onWifiApStatusListence) {
        this.onWifiApStatusListence = onWifiApStatusListence;
    }

    public void setOnConnectionStatusListence(OnConnectionStatusListence onConnectionStatusListence) {
        this.onConnectionStatusListence = onConnectionStatusListence;
    }

    public void setOnScanListence(OnScanListence onScanListence) {
        this.onScanListence = onScanListence;
    }

    public void setOnWifiStatusListence(OnWifiStatusListence onWifiStatusListence) {
        this.onWifiStatusListence = onWifiStatusListence;
    }
}