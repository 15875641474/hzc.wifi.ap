package com.hzc.easy.wifi.ap;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.List;

/**
 * wifi 热点工具
 */
public class WifiUtil {

    private void WifiUtil() {
    }

    public static final String TAG = "WifiUtil";
    private static final String DEFAULT_WIFI_PWD = "12345678";
    private static WifiUtil wifiUtil;
    private Context mContext = null;
    private WifiManager mWifiManager = null;
    public static int WIFI_AP_STATE_DISABLED = 11;

    public static WifiUtil getInstance() {
        if (wifiUtil == null) {
            wifiUtil = new WifiUtil();
        }
        return wifiUtil;
    }


    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context) {
        wifiUtil.mContext = context;
        wifiUtil.mWifiManager = (WifiManager) wifiUtil.mContext.getSystemService(Context.WIFI_SERVICE);
    }

    /**
     * 卸载内存
     */
    public void uninit() {
        wifiUtil.mContext = null;
        wifiUtil.mWifiManager = null;
        wifiUtil = null;
    }

    /**
     * 关闭wifi热点
     */
    public boolean closeWifiAp() {
        if (isWifiApEnabled()) {
            try {
                Method method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
                method.setAccessible(true);
                WifiConfiguration config = (WifiConfiguration) method.invoke(mWifiManager);
                Method method2 = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                method2.invoke(mWifiManager, config, false);
                return true;
            } catch (Exception e) {
                // TODO Auto-generated catch block  
                e.printStackTrace();
            }
        }
        return false;
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
        return mWifiManager.getWifiState() != WifiManager.WIFI_STATE_DISABLED;
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
            return intToIp(ipAddress);
        }
        return "";
    }

    /**
     * 获得服务器wifi地址
     *
     * @return
     */
    public String getServerIpAddress() {
        //获取wifi服务
        DhcpInfo wifiInfo = mWifiManager.getDhcpInfo();
        if (wifiInfo != null) {
            int ipAddress = wifiInfo.serverAddress;
            return intToIp(ipAddress);
        }
        return "";
    }

    /**
     * 启动热点，默认wap2加密
     *
     * @param str
     * @param password
     * @return
     */
    public boolean createWifiAp(String str, String password) {
        return createWifiAp(str, password, WifiAPService.WifiSecurityType.WIFICIPHER_WPA2);
    }

    /**
     * 创建启动WiFi热点
     *
     * @param str
     * @param password
     * @param Type
     * @return
     */
    public boolean createWifiAp(String str, String password, WifiAPService.WifiSecurityType Type) {
        String ssid = str;
        //配置热点信息。
        WifiConfiguration wcfg = new WifiConfiguration();
        wcfg.SSID = new String(ssid);
        wcfg.networkId = 1;
        wcfg.allowedAuthAlgorithms.clear();
        wcfg.allowedGroupCiphers.clear();
        wcfg.allowedKeyManagement.clear();
        wcfg.allowedPairwiseCiphers.clear();
        wcfg.allowedProtocols.clear();

        if (Type == WifiAPService.WifiSecurityType.WIFICIPHER_NOPASS) {
            wcfg.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN, true);
            wcfg.wepKeys[0] = "";
            wcfg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wcfg.wepTxKeyIndex = 0;
        } else if (Type == WifiAPService.WifiSecurityType.WIFICIPHER_WPA) {
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
        } else if (Type == WifiAPService.WifiSecurityType.WIFICIPHER_WPA2) {
            Log.d(TAG, "wifi ap---- wpa2");
            //密码至少8位，否则使用默认密码
            if (null != password && password.length() >= 8) {
                wcfg.preSharedKey = password;
            } else {
                wcfg.preSharedKey = DEFAULT_WIFI_PWD;
            }
            wcfg.hiddenSSID = true;
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
            Method method = mWifiManager.getClass().getMethod("setWifiApConfiguration",
                    wcfg.getClass());
            Boolean rt = (Boolean) method.invoke(mWifiManager, wcfg);
            Log.d(TAG, " rt = " + rt);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
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
                closeWifiAp();
                Thread.sleep(100);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                return false;
            }
        }

        //开启wifi热点
        try {
            Method method1 = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method1.invoke(mWifiManager, null, true);
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
            return WifiAPService.WifiSecurityType.WIFICIPHER_INVALID.ordinal();
        }

        Log.i(TAG, "getSecurity security=" + configuration.allowedKeyManagement);
        if (configuration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.NONE)) {
            return WifiAPService.WifiSecurityType.WIFICIPHER_NOPASS.ordinal();
        } else if (configuration.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
            return WifiAPService.WifiSecurityType.WIFICIPHER_WPA.ordinal();
        } else if (configuration.allowedKeyManagement.get(4)) { //4 means WPA2_PSK
            return WifiAPService.WifiSecurityType.WIFICIPHER_WPA2.ordinal();
        }
        return WifiAPService.WifiSecurityType.WIFICIPHER_INVALID.ordinal();
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
            int wifiid = addWifiConfig(scanResult, pwd);
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
    private int addWifiConfig(ScanResult wifi, String pwd) {
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
     * 计算ip地址
     *
     * @param i
     * @return
     */
    private String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }
}  

