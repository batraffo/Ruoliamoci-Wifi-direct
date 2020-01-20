package com.example.ruoliamoci;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class PlayerService extends Service {

    public class LocalBinder extends Binder {
        PlayerService getService() {
            return PlayerService.this;
        }
    }


    ThreadPlayer tp;

    private boolean created=false;
    private final IBinder mBinder = new LocalBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!created){
            created=true;
            String adress=intent.getExtras().getString("adressu");
            String name=intent.getExtras().getString("name");
            String race=intent.getExtras().getString("race");
            String classs=intent.getExtras().getString("classs");
            String uri=intent.getExtras().getString("uri");
            tp=new ThreadPlayer(adress,name,race,classs,uri);
            Log.d("wei",name+classs+race+ " "+uri);
            tp.start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tp.interrupt();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("Playersevice","it's binding");
        return mBinder;
    }

    private class ThreadPlayer extends Thread{

        String bossAdress;
        String name,race,classs;
        Uri u=null;

        public ThreadPlayer(String adress,String name,String race,String classs,String uri){
            bossAdress=adress;
            this.name=name;
            this.race=race;
            this.classs=classs;
            if(uri!=null)
                u=Uri.parse(uri);
        }

        @Override
        public void run() {
            Socket socket=new Socket();
            LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(getApplicationContext());
            Log.d("HELLO","i'm the player's thread");
            byte [] buf=new byte[1024];
            try {
                socket.setReuseAddress(true);
                socket.bind(null);
                socket.connect(new InetSocketAddress(bossAdress,RuoliamociUtilities.PORT_NUMBER));
                Log.d("Client Socket", "connected");
                OutputStream stream = socket.getOutputStream();
                InputStream input=socket.getInputStream();
                if(u!=null){//we have image
                    Log.d("ThreadPg","here");
                    stream.write("yes".getBytes());
                    input.read();
                    stream.write((name+"\0"+race+"\0"+classs+"\0").getBytes());
                    input.read();
                    //copyfile
                    InputStream is = null;
                    try {
                        is = getApplicationContext().getContentResolver().openInputStream(u);
                    } catch (FileNotFoundException e) {
                        Log.d(PlayerActivity.TAG, e.toString());
                    }
                    RuoliamociUtilities.copyFile(is,stream);
                    is.close();
                    input.read();
                }
                else{
                    stream.write("no".getBytes());
                    input.read();
                    stream.write((name+"\0"+race+"\0"+classs+"\0").getBytes());
                    input.read();
                }
                socket.setSoTimeout(1000);
                int num=-42;
                String stringa;
                while(true) {
                    stream.write("hi!".getBytes());
                    try {
                        num = input.read(buf);
                    }
                    catch (SocketTimeoutException e){
                        e.printStackTrace();
                    }
                    if(num>0) {
                        stringa = Charset.forName("UTF-8").decode(ByteBuffer.wrap(buf)).toString().substring(0, num);
                        switch (stringa){
                            case "disegnino":
                                stream.write("ok".getBytes());
                                try {
                                    input.read();
                                }
                                catch (SocketTimeoutException e){
                                    e.printStackTrace();
                                    break;
                                }

                                final File f = new File(getApplicationContext().getExternalFilesDir("ruoliamociDrawing"),
                                        "Draw" +name+ System.currentTimeMillis()
                                                + ".png");

                                File dirs = new File(f.getParent());
                                if (!dirs.exists())
                                    dirs.mkdirs();
                                f.createNewFile();

                                Log.d("ThreadPlayer","drawing incoming");

                                FileOutputStream effe=new FileOutputStream(f);

                                socket.setSoTimeout(500);
                                RuoliamociUtilities.copyFile(input,effe);
                                socket.setSoTimeout(1000);

                                effe.close();

                                stream.write(0);

                                Intent intent=new Intent();
                                intent.putExtra("stringa",Uri.fromFile(f).toString());
                                intent.setAction("newDrawing");
                                broadcaster.sendBroadcast(intent);
                                break;
                            case "data":
                                stream.write("ok".getBytes());
                                byte [] b= new byte[8];
                                try {
                                    input.read(b);
                                }
                                catch (SocketTimeoutException e){
                                    e.printStackTrace();
                                    break;
                                }
                                long time=RuoliamociUtilities.bytesToLong(b);
                                Intent intent1=new Intent();
                                intent1.putExtra("data",time);
                                intent1.setAction("newDate");
                                broadcaster.sendBroadcast(intent1);
                                stream.write(0);
                                break;
                            default:
                                Log.d("Thread pg", "h e r e: " + stringa);
                        }
                    }
                    else
                        Log.d("ThreadPg","strange things happen");
                    ThreadPlayer.sleep(1000);
                    num=-42;
                }
            }


            catch (IOException e){
                e.printStackTrace();
                try {
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                return;
            } catch (InterruptedException e) {
                e.printStackTrace();
                try {
                    OutputStream stream = socket.getOutputStream();
                    Log.d("thread player","interrupted");
                    stream.write("goodbye0123".getBytes());//not always send with some phone

                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                return;
            }
        }
    }
}