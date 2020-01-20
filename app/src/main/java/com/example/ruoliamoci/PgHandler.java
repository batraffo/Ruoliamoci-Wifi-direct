package com.example.ruoliamoci;

import android.os.Handler;
import android.os.Message;


public class PgHandler extends Handler {

    private AppReceiver appReceiver;
    public PgHandler(AppReceiver receiver) {
        appReceiver = receiver;
    }


    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        appReceiver.onReceiveResult(msg);
    }


    public interface AppReceiver {
        void onReceiveResult(Message message);
    }
}
