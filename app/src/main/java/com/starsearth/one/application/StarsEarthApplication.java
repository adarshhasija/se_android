package com.starsearth.one.application;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.view.accessibility.AccessibilityManager;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.FirebaseDatabase;
import com.starsearth.one.R;
import com.starsearth.one.domain.User;

import java.util.List;

/**
 * Created by faimac on 11/28/16.
 */

public class StarsEarthApplication extends Application {

    private User firebaseUser;
    private FirebaseAnalytics firebaseAnalytics;
    private AppEventsLogger facebookAnalytics; //Facebook analytics

    public FirebaseAnalytics getFirebaseAnalytics() {
        return firebaseAnalytics;
    }

    public AppEventsLogger getFacebookAnalytics() {
        return facebookAnalytics;
    }

    public Bundle getUserPropertiesAccessibility() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("talkback_enabled", isTalkbackOn());
        bundle.putBoolean("magnification_enabled", isMagnificationOn());
        return bundle;
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

    public void updateFacebookUserProperties(String userId) {
        AppEventsLogger.setUserID(userId);

        Bundle user_props = new Bundle();
        user_props.putBoolean("talkback_enabled", isTalkbackOn());
        user_props.putBoolean("magnification_enabled", isMagnificationOn());
        AppEventsLogger.updateUserProperties(user_props, null);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        initializeFirebaseAnalytics();
        initializeFacebookAnalytics();
    }

    private void initializeFirebaseAnalytics() {
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    private void initializeFacebookAnalytics() {
        FacebookSdk.setApplicationId(getResources().getString(R.string.facebook_app_id));
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        facebookAnalytics = AppEventsLogger.newLogger(this);
    }

    public List<String> getAccessibilityServiceName() {
        List<String> result = null;
        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> list = am.getEnabledAccessibilityServiceList(-1);
        if (list != null) {
            for (AccessibilityServiceInfo info : list) {
                ResolveInfo resolveInfo = info.getResolveInfo();
                if (resolveInfo != null) {
                    ServiceInfo serviceInfo = resolveInfo.serviceInfo;
                    if (serviceInfo != null) {
                        result.add(serviceInfo.name);
                    }
                }

            }
        }

        return result;
    }

    public boolean isTalkbackOn() {
        boolean result = false;
        List<String> names = getAccessibilityServiceName();
        if (names != null) {
            for (String name : names) {
                if (name.contains("TalkBack")) {
                    result = true;
                }
            }
        }
        return result;
    }

    public boolean isMagnificationOn() {
        boolean result = false;
        List<String> names = getAccessibilityServiceName();
        if (names != null) {
            for (String name : names) {
                if (name.contains("Magnification")) {
                    result = true;
                }
            }
        }
        return result;
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

    public User getFirebaseUser() {
        return firebaseUser;
    }

    public void setFirebaseUser(User firebaseUser) {
        this.firebaseUser = firebaseUser;
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
