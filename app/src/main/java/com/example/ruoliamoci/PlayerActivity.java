package com.example.ruoliamoci;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class PlayerActivity extends AppCompatActivity {

    public static final String TAG = "pg";
    String bossuAdressu;

    String name,race,classs,uri;

    ImageView image;

    private WiFiDirectBroadcastReceiver receiver;
    private BroadcastReceiver recD;//for receiving drawing from the service
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private final IntentFilter intentFilter = new IntentFilter();
    private final IntentFilter localIntentFilter = new IntentFilter();
    private boolean serviceStarted=false;
    private boolean isDialogShowing=false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layoutplayer);
        image=findViewById(R.id.hero_image);

        bossuAdressu=getIntent().getStringExtra("address");


        localIntentFilter.addAction("newDrawing");
        localIntentFilter.addAction("newDate");

        // Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        recD=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case "newDrawing":
                        Log.d("PlayerAc", "new Drawing");
                        String uri = intent.getStringExtra("stringa");
                        ((FragmentPlayer) getSupportFragmentManager().findFragmentById(R.id.frag_player)).addElement(uri);
                       break;
                    case "newDate":
                        Log.d("PlayerAc", "new Date");
                        RuoliamociUtilities.setDdEvent(getApplicationContext(),intent.getLongExtra("data",0));
                        break;
                    default:
                        Log.d("PlayerAc","this is unexpected");
                }
            }
        };

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        if(savedInstanceState==null) {
            openDialog();//the dialog for pg creation
        }
        else{
            serviceStarted=savedInstanceState.getBoolean("service");
            isDialogShowing=savedInstanceState.getBoolean("dialog");
            name=savedInstanceState.getString("name");
            race=savedInstanceState.getString("race");
            classs=savedInstanceState.getString("classs");
            uri=savedInstanceState.getString("uri");

            if(!serviceStarted){
                openDialog();
            }
            else {
                justSet(name,race,classs,uri);
            }
        }
    }

    private void openDialog() {
        if(!isDialogShowing) {
            PgDialogFragment pdf = new PgDialogFragment();
            isDialogShowing=true;
            pdf.setCancelable(false);
            pdf.show(getSupportFragmentManager(), "DialogFragmentPg");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.menu_player, m);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.exiter:
                disconnect();
                return true;
            default:
                super.onOptionsItemSelected(item);
        }
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((recD),
                localIntentFilter
        );//get things from playerservice
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(recD);
        super.onStop();
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
    public void onBackPressed(){
        super.onBackPressed();
        disconnect();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("service",serviceStarted);
        outState.putBoolean("dialog",isDialogShowing);
        outState.putString("name",name);
        outState.putString("race",race);
        outState.putString("classs",classs);
        outState.putString("uri",uri);

    }

    public void disconnect() {
        Log.d(TAG,"is closing its doors");
        closeSocket();
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
                finish();
            }

            @Override
            public void onSuccess(){
                finish();
            }

        });
    }

    private void closeSocket() {
        Intent intent = new Intent(this, PlayerService.class);
        stopService(intent);
    }

    private void setService(String name, String race, String classs, Uri uri) {
        serviceStarted=true;
        Intent intent = new Intent(this, PlayerService.class);
        intent.putExtra("adressu",bossuAdressu);
        intent.putExtra("name",name);
        intent.putExtra("race",race);
        intent.putExtra("classs",classs);
        if(uri!=null)
            intent.putExtra("uri",uri.toString());
        startService(intent);
    }


    /**
     * called by dialog for set on screen the pg and send it to dm
     *
     * @param name
     * @param race
     * @param classs
     * @param uri
     */
    public void setAndSend(String name, String race, String classs, Uri uri) {
        if(uri!=null){
            RuoliamociUtilities.setImage(uri,image,this);
            this.uri=uri.toString();
        }
        Log.d("wewe",name+" "+race+" "+classs);
        if(name.equals(""))
            name="noValue";
        if(race.equals(""))
            race="noValue";
        if(classs.equals(""))
            classs="noValue";

        this.name=name;
        this.race=race;
        this.classs=classs;

        ((TextView)findViewById(R.id.hero_name)).setText(name);
        ((TextView)findViewById(R.id.hero_class_race)).setText(race+" "+classs);
        setService(name,race,classs,uri);
    }

    /**
     * set on screen after i recover the saved bundle
     * @param name
     * @param race
     * @param classs
     * @param uri
     */
    public void justSet(String name, String race, String classs, String uri){
        if(uri!=null){
            RuoliamociUtilities.setImage(Uri.parse(uri),image,this);
            this.uri=uri;
        }
        Log.d("wewe",name+" "+race+" "+classs);
        if(name.equals(""))
            name="noValue";
        if(race.equals(""))
            race="noValue";
        if(classs.equals(""))
            classs="noValue";

        this.name=name;
        this.race=race;
        this.classs=classs;

        ((TextView)findViewById(R.id.hero_name)).setText(name);
        ((TextView)findViewById(R.id.hero_class_race)).setText(race+" "+classs);
    }

    /**
     * dialog used to show the drawing
     */
    public static class DrawDialog extends DialogFragment {

        View mView;
        ImageView image;
        Uri u;

        public void setUri(Uri u){
            this.u=u;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mView = inflater.inflate(R.layout.dialogimage, container, false);
            image=mView.findViewById(R.id.imagine);
            if(savedInstanceState!=null)
                u=Uri.parse(savedInstanceState.getString("uri"));
            image.setImageURI(u);
            return mView;
        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putString("uri",u.toString());
        }
    }

}
