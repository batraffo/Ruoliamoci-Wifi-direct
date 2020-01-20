package com.example.ruoliamoci;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.ListFragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.net.InetAddress;

/*
  sorry for my english, there will be many grammatical errors in all my code
 */

public class MainActivity extends AppCompatActivity implements WifiP2pManager.ChannelListener, WifiP2pManager.ConnectionInfoListener{

    public static final String TAG = "main";
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION =1001 ;

    private WiFiDirectBroadcastReceiver receiver;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private final IntentFilter intentFilter = new IntentFilter();



    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;

    private Button b;

    private String bossAdress;

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
                if  (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "Fine location permission is not granted!");
                    finish();
                }
                if  (grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "Write permission is not granted!");
                    finish();
                }
                if(grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "Write calendar permission is not granted!");
                    finish();
                }
                if(grantResults[3] != PackageManager.PERMISSION_GRANTED) {
                     Log.e(TAG, "Read calendar permission is not granted!");
                     finish();
                }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);//i can't see the ip without

        // Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        //here i ask for permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.WRITE_CALENDAR)
                     !=PackageManager.PERMISSION_GRANTED  ||
                checkSelfPermission(Manifest.permission.READ_CALENDAR)
                        !=PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.WRITE_CALENDAR,Manifest.permission.READ_CALENDAR},
                    MainActivity.PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION);
            // After this point you wait for callback in
            // onRequestPermissionsResult(int, String[], int[]) overridden method
        }


        b=findViewById(R.id.creagruppo);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isWifiP2pEnabled) {
                    Toast.makeText(MainActivity.this, R.string.p2p_off_warning,
                            Toast.LENGTH_SHORT).show();
                    return ;
                }

                manager.createGroup(channel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {

                        Toast.makeText(MainActivity.this, "group made.",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {

                        Toast.makeText(MainActivity.this, "group fail. use 'Disconnect from everything' ",
                                Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.menu_main, m);
        return true;
    }

    /** register the BroadcastReceiver with the intent values to be matched */
    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this,TAG);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.atn_direct_enable:
                if (manager != null && channel != null) {

                    //I open the wifi settings
                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                } else {
                    Log.e(TAG, "channel or manager is null");
                }
                return true;
            case R.id.atn_direct_discover:
                if (!isWifiP2pEnabled) {
                    Toast.makeText(MainActivity.this, R.string.p2p_off_warning,
                            Toast.LENGTH_SHORT).show();
                    return true;
                }
                ((DeviceFragment)getSupportFragmentManager().findFragmentById(R.id.frag_list)).onInitiateDiscovery();
                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this, "Discovery Initiated",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(MainActivity.this, "Discovery Failed : " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            case R.id.menu1:
                disconnect();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void disconnect() {
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);

            }

            @Override
            public void onSuccess() {
                resetData();
            }

        });
    }

    public void resetData() {
        Log.d(TAG,"it's about to restart the data");

        DeviceFragment dlf= (DeviceFragment) getSupportFragmentManager().findFragmentById(R.id.frag_list);

        dlf.clearPeers();
        dlf.setImConnecting(false);
        dlf.setProgress(false);
        Log.d("here we go","mamma mia");
    }

    public void connect(WifiP2pConfig config){
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us with onConnectionInfoAvailible
            }

            @Override
            public void onFailure(int reason) {
                resetData();
                Toast.makeText(MainActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onChannelDisconnected() {
        // we will try once more
        if (manager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this,
                    "Severe! Channel is probably lost permanently. Try Disable/Re-Enable P2P and restart the app.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        // InetAddress from WifiP2pInfo struct.

        if(info.groupOwnerAddress!=null) {//some phones are stupid
            String adress1 = info.groupOwnerAddress.getHostAddress();
            String adressfixed = info.groupOwnerAddress.getHostName();
            if (adress1 == null)
                bossAdress = adressfixed;
            else
                bossAdress = adress1;
        }

        // After the group negotiation, we can determine the group owner
        // (server).
        if (info.groupFormed && info.isGroupOwner) {
            // Do whatever tasks are specific to the group owner.
            // One common case is creating a group owner thread and accepting
            // incoming connections.
            Intent myIntent= new Intent(this, DmActivity.class);
            startActivity(myIntent);
        } else if (info.groupFormed) {
            // The other device acts as the peer (client). In this case,
            // you'll want to create a peer thread that connects
            // to the group owner.
            if(bossAdress!=null) {
                Intent myIntent = new Intent(this, PlayerActivity.class);
                myIntent.putExtra("address", bossAdress);
                startActivity(myIntent);
            }

        }
    }
}
