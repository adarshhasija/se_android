package com.starsearth.one.domain;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.starsearth.one.BuildConfig;
import com.starsearth.one.R;
import com.starsearth.one.application.StarsEarthApplication;

public class Analytics {

    private Context mContext;
    private FirebaseAnalytics firebaseAnalytics;
    private AppEventsLogger facebookAnalytics;


    public Analytics(Context context) {
        this.mContext = context;
        if (!BuildConfig.DEBUG) {
            initializeFirebaseAnalytics();
            initializeFacebookAnalytics();
        }
    }

    private void initializeFirebaseAnalytics() {
        firebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
    }

    private void initializeFacebookAnalytics() {
        FacebookSdk.setApplicationId(mContext.getResources().getString(R.string.facebook_app_id));
        FacebookSdk.sdkInitialize(mContext);
        AppEventsLogger.activateApp(mContext);
        facebookAnalytics = AppEventsLogger.newLogger(mContext);
    }

    public void logActionEvent(String eventName, Bundle bundle) {
        if (firebaseAnalytics != null) {
            firebaseAnalytics.logEvent(eventName, bundle);
        }
        if (facebookAnalytics != null) {
            facebookAnalytics.logEvent(eventName, bundle);
        }
    }

    public void logActionEvent(String eventName, Bundle bundle, int score) {
        if (firebaseAnalytics != null) {
            firebaseAnalytics.logEvent(eventName, bundle);
        }
        if (facebookAnalytics != null) {
            facebookAnalytics.logEvent(eventName, score, bundle);
        }
    }

    public void logFragmentViewEvent(String fragmentName, Activity activity) {
        if (firebaseAnalytics != null) {
            firebaseAnalytics.setCurrentScreen(activity, fragmentName, null);
        }
        if (facebookAnalytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString("content", fragmentName);
            facebookAnalytics.logEvent(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, bundle);
        }
    }

    public void updateUserAnalyticsInfo(String userId) {
        updateAnalyticsUserId(userId);
        updateUserProperties();
    }

    public void updateAnalyticsUserId(String userId) {
        if (firebaseAnalytics != null) {
            firebaseAnalytics.setUserId(userId);
        }
        if (facebookAnalytics != null) {
            AppEventsLogger.setUserID(userId);
        }
    }

    private void updateUserProperties() {
        Accessibility accessibility = ((StarsEarthApplication) mContext).getAccessibility();
        Bundle user_props = accessibility.getUserPropertiesAccessibility();
        if (firebaseAnalytics != null && user_props != null) {
            for (String key : user_props.keySet()) {
                firebaseAnalytics.setUserProperty(key, user_props.get(key).toString()); //must be a string
            }
        }
        if (facebookAnalytics != null) {
            AppEventsLogger.updateUserProperties(user_props, null);
        }
    }
}
