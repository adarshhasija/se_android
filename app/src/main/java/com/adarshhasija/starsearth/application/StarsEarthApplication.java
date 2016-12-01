package com.adarshhasija.starsearth.application;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.view.accessibility.AccessibilityManager;

import com.adarshhasija.starsearth.R;

/**
 * Created by faimac on 11/28/16.
 */

public class StarsEarthApplication extends Application {

    public boolean isTalkbackOn() {
        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        //Log.d(LOG_TAG, "************ACCESSIBILITY ENABLED: "+am.isEnabled());
        //Log.d(LOG_TAG, "*************TOUCH EXPLORATION ENABLED :"+am.isTouchExplorationEnabled());
        return false;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void vibrate(long timeMillis) {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(timeMillis);
    }

    public void showNoInternetDialog(Context context) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(R.string.error);
        alertDialog.setMessage(R.string.no_internet);
        alertDialog.setNeutralButton(android.R.string.ok, null);
        alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
        alertDialog.show();
    }

}
