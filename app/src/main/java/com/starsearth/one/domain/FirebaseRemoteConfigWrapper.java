package com.starsearth.one.domain;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.starsearth.one.BuildConfig;
import com.starsearth.one.R;

public class FirebaseRemoteConfigWrapper {

    private com.google.firebase.remoteconfig.FirebaseRemoteConfig mFirebaseRemoteConfig;

    public FirebaseRemoteConfigWrapper() {
        mFirebaseRemoteConfig = com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings.Builder configSettingsBuilder = new FirebaseRemoteConfigSettings.Builder();
        if (BuildConfig.DEBUG) {
            configSettingsBuilder.setDeveloperModeEnabled(BuildConfig.DEBUG);
        }
        FirebaseRemoteConfigSettings configSettings = configSettingsBuilder.build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
    }

    public void updateRemoteConfigs() {
        Task<Void> task;
        if (BuildConfig.DEBUG) {
            task = mFirebaseRemoteConfig.fetch(1800);
        }
        else {
           task = mFirebaseRemoteConfig.fetch();
        }

        //mFirebaseRemoteConfig.fetch()
        task
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {


                            // After config data is successfully fetched, it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.activateFetched();
                        }
                    }
                });
    }

    public String get(String key) {
        return mFirebaseRemoteConfig.getString(key);
    }
}
