package ap.wifi.easy.hzc.com.hzceasywifiap;

import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hzc.easy.wifi.ap.HzcWifiAPService;

import java.util.ArrayList;
import java.util.List;

public class ActivityMain extends AppCompatActivity {

    private android.widget.Button btnopen;
    private android.widget.Button btncreate;
    private Button btnclose;
    private Button btnscan;
    private android.support.v7.widget.RecyclerView recyclerview;
    private android.widget.EditText etname;
    private android.widget.EditText etpwd;
    private Adapter adapter;
    HzcWifiAPService hzcWifiAPService = null;
    private Button btnapclose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.btnapclose = (Button) findViewById(R.id.btn_ap_close);
        this.etpwd = (EditText) findViewById(R.id.et_pwd);
        this.etname = (EditText) findViewById(R.id.et_name);
        this.recyclerview = (RecyclerView) findViewById(R.id.recyclerview);
        this.btnscan = (Button) findViewById(R.id.btn_scan);
        this.btnclose = (Button) findViewById(R.id.btn_close);
        this.btncreate = (Button) findViewById(R.id.btn_create);
        this.btnopen = (Button) findViewById(R.id.btn_open);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter();
        recyclerview.setAdapter(adapter);
        hzcWifiAPService = new HzcWifiAPService(this);
        hzcWifiAPService.setOnConnectionStatusListence(new HzcWifiAPService.OnConnectionStatusListence() {
            @Override
            public void onConnectioned(WifiInfo wifiInfo, NetworkInfo networkInfo) {
                if (connectionWifi != null && wifiInfo.getBSSID() == connectionWifi.BSSID) {
                    hzcWifiAPService.doUnRegisterListance();
                    Toast.makeText(ActivityMain.this, "connection success", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onDisConnected(NetworkInfo networkInfo) {

            }
        });
        hzcWifiAPService.setOnScanListence(new HzcWifiAPService.OnScanListence() {
            @Override
            public void onScaning() {

            }

            @Override
            public void onFind(List<ScanResult> devicelist) {
                Toast.makeText(ActivityMain.this, "done scan", Toast.LENGTH_SHORT).show();
                datalist.clear();
                datalist.addAll(devicelist);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onScanDone() {

            }
        });
        hzcWifiAPService.setOnWifiApStatusListence(new HzcWifiAPService.OnWifiApStatusListence() {
            @Override
            public void onOpened() {
                Log.e("ActivityMain","AP was create");
                Toast.makeText(ActivityMain.this, "ap was create", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onClosed() {
                Toast.makeText(ActivityMain.this, "ap was close", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void error() {

            }
        });
        //注册wifi状态改变事件
        hzcWifiAPService.doRegisterListance();

        //关闭热点
        btnapclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hzcWifiAPService.doCloseWifiAp();
            }
        });
        //开启wlan
        btnopen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!hzcWifiAPService.isWifiEnabled()) {
                    hzcWifiAPService.setWifiEnable(true);
                }
            }
        });
        //关闭wlan
        btnclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hzcWifiAPService.isWifiEnabled()) {
                    hzcWifiAPService.setWifiEnable(false);
                }
            }
        });
        //创建热点
        btncreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = etname.getText().toString();
                String pwd = etpwd.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    name = "热点1";
                }
                if (TextUtils.isEmpty(pwd)) {
                    pwd = "12345678";
                }
                hzcWifiAPService.doCreateWifiAp(name, pwd);
            }
        });
        //搜索热点
        btnscan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hzcWifiAPService.doStartScan();
            }
        });
    }

    List<ScanResult> datalist = new ArrayList<>();
    ScanResult connectionWifi;

    class Holder extends RecyclerView.ViewHolder {
        TextView tvDeviceName;

        public Holder(View itemView) {
            super(itemView);
            tvDeviceName = (TextView) itemView.findViewById(R.id.tvDeviceName);
        }
    }

    class Adapter extends RecyclerView.Adapter<Holder> {

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new Holder(getLayoutInflater().inflate(R.layout.item_wifi_device, parent, false));
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            holder.tvDeviceName.setText(datalist.get(position).SSID);
            holder.itemView.setTag(String.valueOf(position));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    connectionWifi = datalist.get(Integer.parseInt(v.getTag().toString()));
                    hzcWifiAPService.doConnection(connectionWifi, "12345678");
                }
            });
        }

        @Override
        public int getItemCount() {
            return datalist.size();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
