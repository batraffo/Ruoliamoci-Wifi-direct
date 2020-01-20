package com.example.ruoliamoci;

import android.content.Context;
import android.net.Uri;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ThreadDm extends Thread {

    private static final int NEW_PG = 42 ;
    private static final int KILL_PG =69 ;
    Socket client;
    int pos;
    PlayerCharacter myPg;
    Context context;
    Messenger messenger;
    ConcurrentLinkedQueue<Operation> clq;

    public ThreadDm(Socket client, ConcurrentLinkedQueue<Operation> clq, int pos, Context context, Messenger messenger){
        this.client=client;
        this.pos=pos;
        this.context=context;
        this.messenger=messenger;
        this.clq=clq;
    }

    @Override
    public void run() {
        boolean receiveinfo = false;//set true after i receive the request of send pg profile, next read will be that
        boolean hasphoto = false;
        int num;
        byte [] buf=new byte[1024];
        Operation op;
        while (true) {
            Log.d("wewe"+pos, "i'm the communicating thread");
            try {
                client.setSoTimeout(7000);
                InputStream inputstream = client.getInputStream();

                try {
                    num=inputstream.read(buf);
                } catch (SocketTimeoutException e){
                    Log.d("Giocatore"+pos,"è vicino alla sua morte #RIP");

                    try {
                        num=inputstream.read(buf);
                    }catch (SocketTimeoutException e1){
                        Log.d("Dying"+pos,"arrirariaririaarrivederci");
                        closeThis(client);
                        return;
                    }
                }

                Log.d("ciao"+pos,num+" here");
                if(num==-1){
                    Log.d("Rip "+pos,"arrirariaririaarrivederci");
                    closeThis(client);
                    return;
                }
                String stringa = Charset.forName("UTF-8").decode(ByteBuffer.wrap(buf)).toString().substring(0,num);

                if(!receiveinfo) {//I don't have to get stuff from the client

                    if (stringa.equals("goodbye0123")) {
                        Log.d("thread" + pos, "sta per ripperare in ripperonis");
                        closeThis(client);
                        return;
                    }

                    if (stringa.equals("yes")) {//connection with image
                        OutputStream stream = client.getOutputStream();
                        stream.write(0);//roger
                        receiveinfo = true;
                        hasphoto=true;
                    }

                    if (stringa.equals("no")) {//connection without image
                        OutputStream stream = client.getOutputStream();
                        stream.write(0);//roger
                        receiveinfo = true;
                        hasphoto=false;
                    }

                    Log.d("ThreadString" + pos, stringa);
                }

                else{//read the pg info
                    receiveinfo=false;
                    StringTokenizer st = new StringTokenizer(stringa, "\0");
                    String name = null;
                    String race = null;
                    String classs = null;
                    Uri uri=null;
                    try {
                        name = st.nextToken();
                        race = st.nextToken();
                        classs = st.nextToken();
                    } catch (NoSuchElementException e) {
                        name = "Brancaleone da Norcia";//one of the greatest italian hero
                        race = "Umano";
                        classs = "Cavaliere";
                    }

                    Log.d("thread dm "+pos,"pg: "+name+" "+race+" "+classs);
                    OutputStream stream = client.getOutputStream();
                    stream.write(0);//ok bro


                    if(hasphoto){
                        Log.d("ThreadDM"+pos,"new photo for me yay");
                        //readfile
                        final File f = new File(context.getExternalFilesDir("ruoliamociPgPic"),
                                "PgPic" +name+ System.currentTimeMillis()
                                        + ".jpg");

                        File dirs = new File(f.getParent());
                        if (!dirs.exists())
                            dirs.mkdirs();
                        f.createNewFile();

                        Log.d("TheradDm"+pos,"here your photo");

                        FileOutputStream effe=new FileOutputStream(f);

                        client.setSoTimeout(500);
                        RuoliamociUtilities.copyFile(inputstream,effe);
                        client.setSoTimeout(7000);

                        effe.close();
                        uri=Uri.fromFile(f);
                        stream.write(0);
                    }
                    else
                        Log.d("threadDM"+pos,"has no photo");

                    myPg=new PlayerCharacter(name,classs,race,uri,pos);//my pg
                    Message msg = new Message();
                    msg.obj = myPg;
                    msg.what = NEW_PG;
                    msg.arg1=pos;
                    messenger.send(msg);
                }

                op=clq.poll();
                if(op!=null){
                    OutputStream stream = client.getOutputStream();
                    switch (op.distaip){
                        case TEST:
                            Log.d("Thread Dm"+pos,"test");
                            stream.write("a e s t h e t i c".getBytes());//i ' m    f u n n y
                            break;
                        case OK:
                            Log.d("threaddm"+pos,"well yes");
                            break;
                        case FILE:
                            Log.d("ThreadDm"+pos,"send disegnino");
                            stream.write("disegnino".getBytes());
                            int n;
                            try {
                                n=inputstream.read(buf);
                            }
                            catch (SocketTimeoutException e){
                                e.printStackTrace();
                                break;
                            }

                            if(n>0){
                                String s = Charset.forName("UTF-8").decode(ByteBuffer.wrap(buf)).toString().substring(0, n);
                                Log.d("ThreadDm"+pos+"file","this is the string: "+s);
                                if(!s.equals("ok")){//never happened but who know
                                    try {
                                        inputstream.read(buf);
                                    }
                                    catch (SocketTimeoutException e){
                                        e.printStackTrace();
                                        break;
                                    }
                                }
                            }
                            else
                                break;

                            stream.write(0);

                            InputStream is=null;
                            Uri u=Uri.fromFile(op.f);

                            try {
                                is = context.getContentResolver().openInputStream(u);
                            } catch (FileNotFoundException e) {
                                Log.d(PlayerActivity.TAG, e.toString());
                            }
                            RuoliamociUtilities.copyFile(is,stream);
                            is.close();
                            inputstream.read();
                            break;
                        case DATE:

                            Log.d("ThreadDm"+pos,"send date");
                            stream.write("data".getBytes());
                            int nu;
                            try {
                                nu=inputstream.read(buf);
                            }
                            catch (SocketTimeoutException e){
                                e.printStackTrace();
                                break;
                            }

                            if(nu>0){
                                String s = Charset.forName("UTF-8").decode(ByteBuffer.wrap(buf)).toString().substring(0, nu);
                                Log.d("ThreadDm"+pos+"date","string "+s);
                                if(!s.equals("ok")){
                                    try {
                                        inputstream.read(buf);
                                    }
                                    catch (SocketTimeoutException e){
                                        e.printStackTrace();
                                        break;
                                    }
                                }
                            }
                            else
                                break;

                            stream.write(RuoliamociUtilities.longToBytes(op.time));

                            inputstream.read();
                            break;
                        default:
                            Log.d("threaddm"+pos,"dunno what is going on");
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                try {
                    closeThis(client);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                Log.d("Thread dm "+pos,"die hard");
                return;
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.d("ThreadDm","something strange happened");
            }

            if(this.isInterrupted()){
                try {
                    Log.d("ThreadDm "+pos,"arrivederci");
                    closeThis(client);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                Log.d("ThreadDm"+pos,"rip");
                return;
            }
        }
    }

    private void closeThis(Socket socket) throws IOException {
        Message msg = new Message();
        msg.what = KILL_PG;
        msg.arg1=pos;
        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        socket.close();
    }

}
