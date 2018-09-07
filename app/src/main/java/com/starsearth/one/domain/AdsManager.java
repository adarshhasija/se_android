package com.starsearth.one.domain;

import android.content.Context;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.starsearth.one.BuildConfig;
import com.starsearth.one.application.StarsEarthApplication;

import java.util.List;
import java.util.Random;

public class AdsManager {

    private InterstitialAd mGoogleInterstitialAd;
    private com.facebook.ads.InterstitialAd mFacebookInterstitalAd;

    ///LISTENERS NOT USED RIGHT NOW
    public AdListener mGoogleAdListener = new AdListener() {
        @Override
        public void onAdLoaded() {
            super.onAdLoaded();

        }

        @Override
        public void onAdClosed() {
            super.onAdClosed();

        }
    };

    public InterstitialAdListener mFacebookAdListener = new InterstitialAdListener() {
        @Override
        public void onInterstitialDisplayed(Ad ad) {

        }

        @Override
        public void onInterstitialDismissed(Ad ad) {

        }

        @Override
        public void onError(Ad ad, AdError adError) {

        }

        @Override
        public void onAdLoaded(Ad ad) {

        }

        @Override
        public void onAdClicked(Ad ad) {

        }

        @Override
        public void onLoggingImpression(Ad ad) {

        }
    };
    ////

    public static boolean shouldGenerateAd(Context context) {
        boolean result = false;
        if (context != null) {
            String moduloString = ((StarsEarthApplication) context).getFirebaseRemoteConfigWrapper().getAdsFrequencyModulo();
            int moduloInt = Integer.parseInt(moduloString);
            Random random = new Random();
            result =  moduloInt > 0 && (random.nextInt(moduloInt) % moduloInt == 0);
        }
        return result;
    }

    public AdsManager(Context context) {
        initializeGoogleAds(context);
        initializeFacebookAds(context);
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
