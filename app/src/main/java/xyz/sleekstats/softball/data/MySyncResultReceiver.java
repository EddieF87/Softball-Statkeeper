package xyz.sleekstats.softball.data;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

public class MySyncResultReceiver extends ResultReceiver {

    private Receiver mReceiver;

    public MySyncResultReceiver(Handler handler) {
        super(handler);
    }

    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle resultData);

    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {

        if (mReceiver != null) {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }

}
