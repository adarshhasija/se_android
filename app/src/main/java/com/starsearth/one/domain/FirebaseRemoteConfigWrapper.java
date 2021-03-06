package com.starsearth.one.domain;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.starsearth.one.BuildConfig;
import com.starsearth.one.R;
import com.starsearth.one.application.StarsEarthApplication;

public class FirebaseRemoteConfigWrapper {

    private Context mContext;
    private com.google.firebase.remoteconfig.FirebaseRemoteConfig mFirebaseRemoteConfig;

    public FirebaseRemoteConfigWrapper(Context context) {
        mContext = context;
        mFirebaseRemoteConfig = com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings.Builder configSettingsBuilder = new FirebaseRemoteConfigSettings.Builder();
        if (BuildConfig.DEBUG) {
            configSettingsBuilder.setDeveloperModeEnabled(BuildConfig.DEBUG);
        }
        FirebaseRemoteConfigSettings configSettings = configSettingsBuilder.build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        updateRemoteConfigs();
    }

    public void updateRemoteConfigs() {
        Task<Void> task;
        if (BuildConfig.DEBUG) {
            task = mFirebaseRemoteConfig.fetch(300);
        }
        else {
           task = mFirebaseRemoteConfig.fetch();
        }

        //mFirebaseRemoteConfig.fetch();
        task
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            // After config data is successfully fetched, it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.activateFetched();
                            ((StarsEarthApplication) mContext).getAnalyticsManager().remoteConfigUpdated();
                        }
                    }
                });
    }

    public String get(String key) {
        return mFirebaseRemoteConfig.getString(key);
    }

    public String getAdsFrequencyModulo() {
        return mFirebaseRemoteConfig.getString("ads_frequency_modulo");
    }

    public String getAds() {
        return mFirebaseRemoteConfig.getString("ads");
    }

    public String getGestureSpamMessage() {
        return mFirebaseRemoteConfig.getString("gesture_spam_message");
    }
}
