package com.example.ruoliamoci;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import androidx.annotation.Nullable;

public class ListenerService extends Service {

    public class LocalBinder extends Binder {
        ListenerService getService() {
            return ListenerService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();
    public ArrayList<ThreadDm> altd=new ArrayList<ThreadDm>();//all the thread that communicate with the players
    public ArrayList<ConcurrentLinkedQueue<Operation>> alclq=new ArrayList<ConcurrentLinkedQueue<Operation>>();//all the queue used by the thread in altd (there is correspondence between the position of the thread and the position of the queue)
    private boolean created=false;
    int pos=0;//I always increase the position every time I create a new thread

    ThreadListener tl=null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("ListenerService","created");
        created=true;
        tl=new ThreadListener();
        tl.start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("ListenerService","binding");
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            closeAllTheSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private void closeAllTheSocket() throws IOException {
        for(int i=0; i<altd.size(); i++)
            altd.get(i).interrupt();//I close the threads that communicate with the players, interrupting an already deactivated thread does nothing
        tl.interrupt();
    }

    public void test(int pos){
        alclq.get(pos).add(new Operation(Operation.Type.TEST));
        Log.d("ListenerService",pos+" added test");
    }

    private class ThreadListener extends Thread{
        @Override
        public void run() {
            ServerSocket sv = null;
            try {
                sv=new ServerSocket();
                sv.setReuseAddress(true);
                sv.bind(new InetSocketAddress(RuoliamociUtilities.PORT_NUMBER));
                Log.d(MainActivity.TAG,"ServerSocket created");
                sv.setSoTimeout(1000);
                while (true){
                    try {
                        Socket client=sv.accept();
                        ConcurrentLinkedQueue<Operation> operations=new ConcurrentLinkedQueue<Operation>();
                        ThreadDm td=new ThreadDm(client,operations,pos,getApplicationContext(),DmActivity.giveMeMessenger());
                        alclq.add(pos,operations);
                        altd.add(pos,td);
                        pos++;
                        td.start();
                    }
                    catch (SocketTimeoutException e){
                        Log.d("Server socket","timeout");
                        if(this.isInterrupted()){
                            Log.d("Server Socket","i die");
                            sv.close();
                            return;
                        }
                    }


                }
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    sv.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                return;
            }
        }
    }

}
