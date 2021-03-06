package pl.temomuko.autostoprace.service.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import javax.inject.Inject;

import pl.temomuko.autostoprace.data.Event;
import pl.temomuko.autostoprace.util.EventUtil;
import pl.temomuko.autostoprace.util.LogUtil;
import pl.temomuko.autostoprace.util.NetworkUtil;

/**
 * Created by Rafał Naniewicz on 24.04.2016.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    private static final String TAG = NetworkChangeReceiver.class.getSimpleName();

    @Inject
    public NetworkChangeReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            LogUtil.i(TAG, "Network status changed.");
            if (NetworkUtil.isConnected(context)) {
                EventUtil.post(new Event.NetworkConnected());
            }
        }
    }
}