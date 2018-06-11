# hzc.wifi.ap
easy to use wifi-ap

## In Gradle
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
        }
}

dependencies {
    implementation 'com.github.15875641474:hzc.wifi.ap:1.0.2'
}
```

### API
* HzcWifiAPService    
Listance the WLAN status
* HzcWifiUtil  
operation the wifi like [connect/create/close]
* more .. see the sourse or desc
### Use
### if you need listance wifi status ,do like that.
```
HzcWifiAPService service = new HzcWifiAPService()
hzcWifiAPService.registerListance();

```
### if you need operation wifi / ap ,do like that
### Step1 
init the tool
```
HzcWifiUtil.getInstance().init(this);
```
### Step 2
call api to do some think
```
HzcWifiUtil.getInstance().*
*closeWifiAp
*isWifiApEnabled
*isWifiEnabled
*createWifiAp
*setWifiEnable
*setWifiApEnabled
*isConnection
*doConnection
```