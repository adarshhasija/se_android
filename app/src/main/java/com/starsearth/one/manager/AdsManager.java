package com.starsearth.one.manager;

import android.content.Context;
import android.os.Bundle;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.starsearth.one.BuildConfig;
import com.starsearth.one.application.StarsEarthApplication;
import com.starsearth.one.domain.Course;
import com.starsearth.one.domain.Result;
import com.starsearth.one.domain.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AdsManager {

    private Context mContext;
    private InterstitialAd mGoogleInterstitialAd;
    private com.facebook.ads.InterstitialAd mFacebookInterstitalAd;

    private void sendAnalytics(String callbackType, String sourcePlatform) {
        Bundle bundle = new Bundle();
        bundle.putString("callback_type", callbackType);
        bundle.putString("source_platform", sourcePlatform);
        StarsEarthApplication application = (StarsEarthApplication) mContext.getApplicationContext();
        application.logActionEvent("ADVERTISEMENT_EVENT", bundle);
    }


    public AdListener mGoogleAdListener = new AdListener() {
        @Override
        public void onAdLoaded() {
            super.onAdLoaded();
            sendAnalytics("AD_LOADED", "Google");
        }

        @Override
        public void onAdClosed() {
            super.onAdClosed();
            sendAnalytics("AD_CLOSED", "Google");
        }

        @Override
        public void onAdImpression() {
            super.onAdImpression();
            sendAnalytics("AD_IMPRESSION", "Google");
        }
    };

    public InterstitialAdListener mFacebookAdListener = new InterstitialAdListener() {
        @Override
        public void onInterstitialDisplayed(Ad ad) {
            sendAnalytics("INTERSTITIAL_DISPLAYED", "Facebook");
        }

        @Override
        public void onInterstitialDismissed(Ad ad) {
            sendAnalytics("INTERSTITIAL_DISMISSED", "Facebook");
        }

        @Override
        public void onError(Ad ad, AdError adError) {
            sendAnalytics("ERROR", "Facebook");
        }

        @Override
        public void onAdLoaded(Ad ad) {
            sendAnalytics("AD_LOADED", "Facebook");
        }

        @Override
        public void onAdClicked(Ad ad) {
            sendAnalytics("AD_CLICKED", "Facebook");
        }

        @Override
        public void onLoggingImpression(Ad ad) {
            sendAnalytics("LOGGING_IMPRESSION", "Facebook");
        }
    };


    public static boolean shouldGenerateAd(Context context, Object teachingContent, List<Result> results) {
        boolean ret = false;
        String ads = ((StarsEarthApplication) context).getFirebaseRemoteConfigWrapper().getAds();
        if (!ads.equalsIgnoreCase("None") && context != null && teachingContent instanceof Course) {
            boolean isAccessibilityUser = ((StarsEarthApplication) context.getApplicationContext()).getAccessibility().isAccessibilityUser();
            boolean isOwnerWantingAds = ((Course) teachingContent).isOwnerWantingAds;
            boolean isAllowed = ((Course) teachingContent).shouldGenerateAd(results);

            ret = !isAccessibilityUser && isOwnerWantingAds && isAllowed;
        }
        else if (!ads.equalsIgnoreCase("None") && context != null && teachingContent instanceof Task) {
            boolean isAccessibilityUser = ((StarsEarthApplication) context.getApplicationContext()).getAccessibility().isAccessibilityUser();
            boolean isOwnerWantingAds = ((Task) teachingContent).isOwnerWantingAds;
            //For Task, isAllowed is decided by a coin toss
            String moduloString = ((StarsEarthApplication) context).getFirebaseRemoteConfigWrapper().getAdsFrequencyModulo();
            int moduloInt = Integer.parseInt(moduloString);
            Random random = new Random();
            boolean isAllowed =  moduloInt > 0 && (random.nextInt(moduloInt) % moduloInt == 0);

            ret = !isAccessibilityUser && isOwnerWantingAds && isAllowed;
        }
        return ret;
    }

    public AdsManager(Context context) {
        initializeGoogleAds(context);
        initializeFacebookAds(context);
        mContext = context;
        setupAdListeners();
    }

    private void setupAdListeners() {
        mGoogleInterstitialAd.setAdListener(mGoogleAdListener);
        mFacebookInterstitalAd.setAdListener(mFacebookAdListener);
    }

    public InterstitialAd getGoogleInterstitialAd() {
        return mGoogleInterstitialAd;
    }

    public com.facebook.ads.InterstitialAd getFacebookInterstitalAd() {
        return mFacebookInterstitalAd;
    }

    private void initializeGoogleAds(Context context) {
        if (BuildConfig.DEBUG) {
            // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
            MobileAds.initialize(context, "ca-app-pub-3940256099942544~3347511713");
            mGoogleInterstitialAd = new InterstitialAd(context);
            mGoogleInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
        } else {
            MobileAds.initialize(context, "ca-app-pub-1378964097701084~9829207692");
            mGoogleInterstitialAd = new InterstitialAd(context);
            mGoogleInterstitialAd.setAdUnitId("ca-app-pub-1378964097701084/1268191394");
        }
    }

    private void initializeFacebookAds(Context context) {
        mFacebookInterstitalAd = new com.facebook.ads.InterstitialAd(context, "2064355667218856_2069620790025677");
    }
}
