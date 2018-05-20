package com.starsearth.one.domain;

import android.content.Context;

import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.starsearth.one.BuildConfig;

public class Ads {

    private InterstitialAd mGoogleInterstitialAd;
    private com.facebook.ads.InterstitialAd mFacebookInterstitalAd;

    public Ads(Context context) {
        initializeGoogleAds(context);
        initializeFacebookAds(context);
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

    public void onDestroy() {
        if (mFacebookInterstitalAd != null) {
            mFacebookInterstitalAd.destroy();
        }
    }
}
