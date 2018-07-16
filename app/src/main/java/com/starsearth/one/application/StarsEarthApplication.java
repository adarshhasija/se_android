package com.starsearth.one.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.database.FirebaseDatabase;
import com.starsearth.one.R;
import com.starsearth.one.domain.Accessibility;
import com.starsearth.one.domain.Ads;
import com.starsearth.one.domain.Analytics;
import com.starsearth.one.domain.FirebaseRemoteConfigWrapper;
import com.starsearth.one.domain.User;

/**
 * Created by faimac on 11/28/16.
 */

public class StarsEarthApplication extends Application {

    private FirebaseRemoteConfigWrapper mFirebaseRemoteConfigWrapper;
    private Analytics mAnalytics;
    private Accessibility mAccessibility;
    private Ads mAds;

    private User user;

    public FirebaseRemoteConfigWrapper getFirebaseRemoteConfigWrapper() {
        return mFirebaseRemoteConfigWrapper;
    }

    public InterstitialAd getGoogleInterstitialAd() {
        InterstitialAd result = null;
        if (mAds != null) {
            result = mAds.getGoogleInterstitialAd();
        }
        return result;
    }

    public com.facebook.ads.InterstitialAd getFacebookInterstitalAd() {
        com.facebook.ads.InterstitialAd result = null;
        if (mAds != null) {
            result = mAds.getFacebookInterstitalAd();
        }
        return result;
    }

    public Analytics getAnalytics() {
        return mAnalytics;
    }

    public Accessibility getAccessibility() {
        return mAccessibility;
    }

    private SharedPreferences preferences;
    private SharedPreferences.OnSharedPreferenceChangeListener spChanged =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

                }
            };

    public void logActionEvent(String eventName, Bundle bundle) {
        if (mAnalytics != null) {
            mAnalytics.logActionEvent(eventName, bundle);
        }
    }

    public void logActionEvent(String eventName, Bundle bundle, int score) {
        if (mAnalytics != null) {
            mAnalytics.logActionEvent(eventName, bundle, score);
        }
    }

    public void logFragmentViewEvent(String fragmentName, Activity activity) {
        if (mAnalytics != null) {
            mAnalytics.logFragmentViewEvent(fragmentName, activity);
        }
    }

    public void updateUserAnalyticsInfo(String userId) {
        if (mAnalytics != null) {
            mAnalytics.updateUserAnalyticsInfo(userId);
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        //preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //preferences.registerOnSharedPreferenceChangeListener(spChanged);

        mFirebaseRemoteConfigWrapper = new FirebaseRemoteConfigWrapper();
        mAnalytics = new Analytics(getApplicationContext());
        mAccessibility = new Accessibility(getApplicationContext());
        mAds = new Ads(getApplicationContext());

        //Skill skill = new Skill("Adarsh", "Hasija", "sample_email@gmail.com", "accessibility");
        //DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        //String key = mDatabase.push().getKey();
        //mDatabase.child("skills").child(key).setValue(skill);
    }

    public String getRemoteConfigAnalytics() {
        return mFirebaseRemoteConfigWrapper.get("analytics");
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public AlertDialog.Builder createAlertDialog(Context context) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        return builder;
    }

}
