package com.example.ruoliamoci;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


public class DmActivity extends AppCompatActivity implements PgHandler.AppReceiver {

    public static final String TAG = "dm";

    ListenerService mService;
    boolean mBound = false;

    private static PgHandler handler;
    private WiFiDirectBroadcastReceiver receiver;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private final IntentFilter intentFilter = new IntentFilter();
    private boolean serviceStarted = false;
    static FragmentDm fdm;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layoutdm);

        fdm = (FragmentDm) getSupportFragmentManager().findFragmentById(R.id.frag_dm);

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
        if (savedInstanceState == null) {
            setService();
        } else {
            serviceStarted = savedInstanceState.getBoolean("service");
            if (!serviceStarted) {
                setService();
            } else
                Log.d(TAG, "re-entered");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, ListenerService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }


    private void setService() {
        if (!serviceStarted) {
            serviceStarted = true;
            Intent intent = new Intent(this, ListenerService.class);
            handler = new PgHandler(this);
            startService(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        MenuInflater mi = getMenuInflater();
        mi.inflate(R.menu.menu_dm, m);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.killer:
                Log.d(TAG, "exiting by -finish session-");
                disconnect();
                return true;
            default:
                super.onOptionsItemSelected(item);
        }
        return false;
    }

    /** register the BroadcastReceiver with the intent values to be matched */
    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this, TAG);
        registerReceiver(receiver, intentFilter);

    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        getSupportFragmentManager().popBackStack();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(connection);
            mBound = false;
        }
        Log.d("DmActivity", "onStop");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("service", serviceStarted);
    }

    public void disconnect() {
        Log.d(TAG, "gooodbye");
        chiudisocket();
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
            }

            @Override
            public void onSuccess() {
                finish();
            }

        });
        finish();
    }

    private void chiudisocket() {
        Intent intent = new Intent(this, ListenerService.class);
        stopService(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("DmActivity", "see ya");
        disconnect();
    }

    //used to open the fragment draw
    public void sendTest(int pos) {
        mService.test(pos);//just 4 debug purpose
        FragmentManager fm = getSupportFragmentManager();

        FragmentTransaction trans = fm.beginTransaction();
        FragmentDraw fd = new FragmentDraw(pos);
        trans.add(R.id.dmlayout, fd);
        fm.popBackStack();//tolgo il frammento di prima se c'era
        trans.addToBackStack(null);
        findViewById(R.id.frag_dm).setVisibility(View.GONE);
        trans.commit();
    }

    public void sendDrawing(Bitmap saveDrawing, int pos) throws IOException {
        Operation e = new Operation(Operation.Type.FILE);
        File f = new File(getExternalFilesDir("ruoliamociBitmap"),
                "Bitmap" + System.currentTimeMillis()
                        + ".png");

        f.createNewFile();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        saveDrawing.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

        //write the bytes in file
        FileOutputStream fos;
        fos = new FileOutputStream(f);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();
        e.setFile(f);
        mService.alclq.get(pos).add(e);
    }

    //date: milliseconds from 01/01/1970
    public void sendDate(long date) {
        RuoliamociUtilities.setDdEvent(getApplicationContext(),date);
        for(int i=0; i<fdm.giocatoriAggiunti; i++){
            Operation op=new Operation(Operation.Type.DATE);
            op.setTime(date);
            mService.alclq.get(i).add(op);
        }
    }

    //given with the bind

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ListenerService.LocalBinder binder = (ListenerService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


    //called by PGHandler
    //all messages will be read after onResume
    @Override
    public void onReceiveResult(Message message) {
        if(fdm==null){//never happened, but no one knows
            Log.e("DmActivity","something bad is happening");
        }
        switch (message.what) {
            case 42:
                Log.d("DmActivity","new user");
                fdm.addList(message.arg1,(PlayerCharacter) message.obj);
                break;
            case 69:
                Log.d("ActivityDm","ariarirariaiririraivederci");
                fdm.removeList(message.arg1);
                break;
            default:
                Log.d("Dmactivity","this was... unexpected");
        }
    }

    //free messenger (used by the threads in the listener for calling this on onreceiveresult)
    public static Messenger giveMeMessenger(){
        return new Messenger(handler);
    }


    public void setDate() {
        CalendarDialog cd=new CalendarDialog();
        cd.show(getSupportFragmentManager(), "DialogDate");
    }

    public static class CalendarDialog extends DialogFragment{

        View mContentView = null;
        CalendarView cw;
        long time;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            time=-1;
            mContentView=inflater.inflate(R.layout.calendardialog,container,false);
            cw=mContentView.findViewById(R.id.calendarView);

            cw.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                @Override
                public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                    Calendar c = Calendar.getInstance();
                    c.set(year, month, dayOfMonth);
                    time = c.getTimeInMillis();
                }
            });

            mContentView.findViewById(R.id.xmlbrutto).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(time!=-1)
                       ((DmActivity)getActivity()).sendDate(time);
                    else
                        ((DmActivity)getActivity()).sendDate(cw.getDate());
                    dismiss();
                }
            });
            return mContentView;
        }
    }

}
