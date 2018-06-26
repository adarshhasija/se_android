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
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.starsearth.one.R;
import com.starsearth.one.domain.Accessibility;
import com.starsearth.one.domain.Ads;
import com.starsearth.one.domain.Analytics;
import com.starsearth.one.domain.FirebaseRemoteConfigWrapper;
import com.starsearth.one.domain.Skill;
import com.starsearth.one.domain.User;

/**
 * Created by faimac on 11/28/16.
 */

public class StarsEarthApplication extends Application implements Application.ActivityLifecycleCallbacks {

    private FirebaseRemoteConfigWrapper mFirebaseRemoteConfigWrapper;
    private Analytics mAnalytics;
    private Accessibility mAccessibility;
    private Ads mAds;

    private User firebaseUser;
    private FirebaseAnalytics firebaseAnalytics;
    private AppEventsLogger facebookAnalytics;

    private InterstitialAd mGoogleInterstitialAd;
    private com.facebook.ads.InterstitialAd mFacebookInterstitalAd;

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

/*    public Bundle getUserPropertiesAccessibility() {
        Bundle bundle = new Bundle();
        bundle.putInt("talkback_enabled", isTalkbackOn()? 1 : 0);
        bundle.putInt("magnification_enabled", isMagnificationServiceOn()? 1 : 0);
        bundle.putInt("select_to_speak_enabled", isSelectToSpeakOn()? 1 : 0);
        bundle.putInt("switch_access_enabled", isSwitchAccessOn()? 1 : 0);
        bundle.putInt("voice_access_enabled", isVoiceAccessOn()? 1 : 0);
        bundle.putInt("braille_back_enabled", isBrailleBackOn()? 1 : 0);
        return bundle;
    }   */

    public void logActionEvent(String eventName, Bundle bundle) {
        if (mAnalytics != null) {
            mAnalytics.logActionEvent(eventName, bundle);
        }

      /*  if (firebaseAnalytics != null) {
            firebaseAnalytics.logEvent(eventName, bundle);
        }
        if (facebookAnalytics != null) {
            facebookAnalytics.logEvent(eventName, bundle);
        }   */
    }

    public void logActionEvent(String eventName, Bundle bundle, int score) {
        if (mAnalytics != null) {
            mAnalytics.logActionEvent(eventName, bundle, score);
        }
      /*  if (firebaseAnalytics != null) {
            firebaseAnalytics.logEvent(eventName, bundle);
        }
        if (facebookAnalytics != null) {
            facebookAnalytics.logEvent(eventName, score, bundle);
        }   */
    }

    public void logFragmentViewEvent(String fragmentName, Activity activity) {
        if (mAnalytics != null) {
            mAnalytics.logFragmentViewEvent(fragmentName, activity);
        }
      /*  if (firebaseAnalytics != null) {
            firebaseAnalytics.setCurrentScreen(activity, fragmentName, null);
        }
        if (facebookAnalytics != null) {
            Bundle bundle = new Bundle();
            bundle.putString("content", fragmentName);
            facebookAnalytics.logEvent(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, bundle);
        }   */
    }

    public void updateUserAnalyticsInfo(String userId) {
        if (mAnalytics != null) {
            mAnalytics.updateUserAnalyticsInfo(userId);
        }
        //updateAnalyticsUserId(userId);
        //updateUserProperties();
    }

    private void updateAnalyticsUserId(String userId) {
        if (mAnalytics != null) {
            mAnalytics.updateAnalyticsUserId(userId);
        }
      /*  if (firebaseAnalytics != null) {
            firebaseAnalytics.setUserId(userId);
        }
        if (facebookAnalytics != null) {
            AppEventsLogger.setUserID(userId);
        }   */
    }

  /*  private void updateUserProperties() {
        Bundle user_props = getUserPropertiesAccessibility();
        if (firebaseAnalytics != null && user_props != null) {
            for (String key : user_props.keySet()) {
                firebaseAnalytics.setUserProperty(key, user_props.get(key).toString()); //must be a string
            }
        }
        if (facebookAnalytics != null) {
            AppEventsLogger.updateUserProperties(user_props, null);
        }
    }   */


    @Override
    public void onCreate() {
        super.onCreate();
        //registerActivityLifecycleCallbacks(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        //preferences = PreferenceManager.getDefaultSharedPreferences(this);
        //preferences.registerOnSharedPreferenceChangeListener(spChanged);


        /*if (!BuildConfig.DEBUG) {
            initializeFirebaseAnalytics();
            initializeFacebookAnalytics();
        }   */
        mFirebaseRemoteConfigWrapper = new FirebaseRemoteConfigWrapper();
        mAnalytics = new Analytics(getApplicationContext());
        mAccessibility = new Accessibility(getApplicationContext());
        mAds = new Ads(getApplicationContext());

        //Skill skill = new Skill("Niharika", "Bora", "sample_email@gmail.com", "HR");
        //DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        //String key = mDatabase.push().getKey();
        //mDatabase.child("skills").child(key).setValue(skill);
    }

    public String getRemoteConfigAnalytics() {
        return mFirebaseRemoteConfigWrapper.get("analytics");
    }

  /*  private void initializeFirebaseAnalytics() {
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    private void initializeFacebookAnalytics() {
        FacebookSdk.setApplicationId(getResources().getString(R.string.facebook_app_id));
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        facebookAnalytics = AppEventsLogger.newLogger(this);
    }   */

 /*   private void initializeGoogleAds() {
        if (BuildConfig.DEBUG) {
            // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
            MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");
            mGoogleInterstitialAd = new InterstitialAd(this);
            mGoogleInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        } else {
            MobileAds.initialize(this, "ca-app-pub-1378964097701084~9829207692");
            mGoogleInterstitialAd = new InterstitialAd(this);
            mGoogleInterstitialAd.setAdUnitId("ca-app-pub-1378964097701084/1268191394");
        }
    }

    private void initializeFacebookAds() {
        mFacebookInterstitalAd = new com.facebook.ads.InterstitialAd(this, "2064355667218856_2069620790025677");
    }   */

/*    public List<String> getAccessibilityEnabledServiceNames() {
        List<String> result = new ArrayList<>();
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
        List<String> names = getAccessibilityEnabledServiceNames();
        if (names != null) {
            for (String name : names) {
                if (name.contains("TalkBack")) {
                    result = true;
                }
            }
        }
        return result;
    }

    public boolean isMagnificationServiceOn() {
        boolean result = false;
        List<String> names = getAccessibilityEnabledServiceNames();
        if (names != null) {
            for (String name : names) {
                if (name.contains("Magnification")) {
                    result = true;
                }
            }
        }
        return result;
    }

    public boolean isSelectToSpeakOn() {
        boolean result = false;
        List<String> names = getAccessibilityEnabledServiceNames();
        if (names != null) {
            for (String name : names) {
                if (name.contains("SelectToSpeak")) {
                    result = true;
                }
            }
        }
        return result;
    }

    public boolean isSwitchAccessOn() {
        boolean result = false;
        List<String> names = getAccessibilityEnabledServiceNames();
        if (names != null) {
            for (String name : names) {
                if (name.contains("SwitchAccess")) {
                    result = true;
                }
            }
        }
        return result;
    }

    public boolean isVoiceAccessOn() {
        boolean result = false;
        List<String> names = getAccessibilityEnabledServiceNames();
        if (names != null) {
            for (String name : names) {
                if (name.contains("VoiceAccess")) {
                    result = true;
                }
            }
        }
        return result;
    }

    public boolean isBrailleBackOn() {
        boolean result = false;
        List<String> names = getAccessibilityEnabledServiceNames();
        if (names != null) {
            for (String name : names) {
                if (name.contains("BrailleBack")) {
                    result = true;
                }
            }
        }
        return result;
    }   */

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

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        //mFirebaseRemoteConfigWrapper.updateRemoteConfigs();
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
