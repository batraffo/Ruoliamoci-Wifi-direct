package com.example.ruoliamoci;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Parcelable;
import android.util.Log;

public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mActivity;
    private DmActivity dmActivity;
    private PlayerActivity pgActivity;
    private String activityChooser;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       Activity activity,String activityChooser) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.activityChooser=activityChooser;
        switch (activityChooser){
            case "main":
                mActivity=(MainActivity) activity;
                break;
            case"dm":
                dmActivity=(DmActivity) activity;
                break;
            case "pg":
                pgActivity=(PlayerActivity) activity;
                break;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled
                setIsWifiP2pEnabled(true);
            } else {
                // Wi-Fi P2P is not enabled
                setIsWifiP2pEnabled(false);
            }


        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers

            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            requestPeers();

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            infoChanged((NetworkInfo)intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO));

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
            updateThisDevice(intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
        }
    }

    private void infoChanged(NetworkInfo ni) {
        switch (activityChooser) {
            case "main":
                if (mManager == null) {
                    return;
                }


                if (ni.isConnected()) {

                    // We are connected with the other device, request connection
                    // info to find group owner IP

                    mManager.requestConnectionInfo(mChannel, mActivity);
                }
                else { //disconnection
                    mActivity.resetData();
                }

                Log.d(MainActivity.TAG, "P2P info changed");
                break;
            default:
                Log.d("WifiBroadcast", "infos changed");
        }
    }

    private void requestPeers() {
        switch (activityChooser){
            case "main":
                if (mManager != null) {
                    mManager.requestPeers(mChannel, (WifiP2pManager.PeerListListener) mActivity.getSupportFragmentManager().findFragmentById(R.id.frag_list));
                }
                Log.d(MainActivity.TAG, "P2P peers changed");
                break;
            default:
                 Log.d("WifiBroadcast","peers changed");
        }
    }


    private void setIsWifiP2pEnabled(boolean b) {
        switch (activityChooser){
            case "main":
                mActivity.setIsWifiP2pEnabled(b);
                break;
            case "dm":
                if(b==false) {
                    Log.d("EXIT","set wifip2penabled");
                    dmActivity.disconnect();
                }
                break;
            case "pg":
                if(b==false) {
                    pgActivity.disconnect();
                }
                break;
            default:
                 Log.d("WifiBroadcast","changed wifi state");
        }

    }

    private void updateThisDevice(Parcelable parcelableExtra) {
        switch (activityChooser){
            case "main":
                mActivity.resetData();
                DeviceFragment fragment = (DeviceFragment) mActivity.getSupportFragmentManager()
                        .findFragmentById(R.id.frag_list);
                fragment.updateThisDevice((WifiP2pDevice) parcelableExtra);
                break;
            case "dm":
                if(((WifiP2pDevice) parcelableExtra).status!=WifiP2pDevice.CONNECTED && ((WifiP2pDevice) parcelableExtra).status!=WifiP2pDevice.AVAILABLE) {
                    Log.d("That's why i die", String.valueOf(((WifiP2pDevice) parcelableExtra).status) +"number of death");
                    dmActivity.disconnect();
                }
                break;
            case "pg":
                if(((WifiP2pDevice) parcelableExtra).status!=WifiP2pDevice.CONNECTED)
                    pgActivity.disconnect();
                break;
            default:
                Log.d("WifiDirect","chaged my state");
        }
    }
}
