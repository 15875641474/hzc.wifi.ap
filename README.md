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
    implementation 'com.github.15875641474:hzc.wifi.ap:1.0.3'
}
```

### API
* HzcWifiAPService
```    
Listance the WLAN status and operation AP/WLAN 
HzcWifiAPService.do*  
HzcWifiAPService.set*
HzcWifiAPService.is*

```
* more .. see the sourse or desc
### Use
```
HzcWifiAPService apService = new HzcWifiAPService(activity) 

``` 
### Step 2
call api to do some think
```
part1. AP About  
apService.doCreateWifiAp(name.pwd)
apService.doCloseWifiAp()
  
part2. WLAN About  
apService.setWifiEnable(boolean)

```

