package com.starsearth.two.managers

import android.content.Context

import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAdListener
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
//import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
//import com.google.android.gms.ads.interstitial.InterstitialAd
import com.starsearth.two.BuildConfig
import com.starsearth.two.application.StarsEarthApplication
import com.starsearth.two.domain.Course
import com.starsearth.two.domain.Result
import com.starsearth.two.domain.SETeachingContent
import com.starsearth.two.domain.Task
import java.util.Random

class AdsManager(private val mContext: Context) {
    private var adRequest: AdRequest.Builder? = null
    //var googleInterstitialAd: InterstitialAd? = null
        //private set
    var facebookInterstitalAd: com.facebook.ads.InterstitialAd? = null
        private set

    private val GOOGLE = "google"
    private val FACEBOOK = "facebook"


  /*  var mGoogleAdListener: AdListener = object : AdListener() {
        override fun onAdLoaded() {
            super.onAdLoaded()
            (mContext.applicationContext as StarsEarthApplication).analyticsManager.sendAnalyticsForAdvertisingEvent("AD_LOADED", GOOGLE)
        }

        override fun onAdClosed() {
            super.onAdClosed()
            (mContext.applicationContext as StarsEarthApplication).analyticsManager.sendAnalyticsForAdvertisingEvent("AD_CLOSED", GOOGLE)
        }

        override fun onAdImpression() {
            super.onAdImpression()
            (mContext.applicationContext as StarsEarthApplication).analyticsManager.sendAnalyticsForAdvertisingEvent("AD_IMPRESSION", GOOGLE)
        }
    }   */

    var mFacebookAdListener: InterstitialAdListener = object : InterstitialAdListener {
        override fun onInterstitialDisplayed(ad: Ad) {
            (mContext.applicationContext as StarsEarthApplication).analyticsManager.sendAnalyticsForAdvertisingEvent("INTERSTITIAL_DISPLAYED", FACEBOOK)
        }

        override fun onInterstitialDismissed(ad: Ad) {
            (mContext.applicationContext as StarsEarthApplication).analyticsManager.sendAnalyticsForAdvertisingEvent("INTERSTITIAL_DISMISSED", FACEBOOK)
        }

        override fun onError(ad: Ad, adError: AdError) {
            (mContext.applicationContext as StarsEarthApplication).analyticsManager.sendAnalyticsForAdvertisingEvent("ERROR", FACEBOOK)
        }

        override fun onAdLoaded(ad: Ad) {
            (mContext.applicationContext as StarsEarthApplication).analyticsManager.sendAnalyticsForAdvertisingEvent("AD_LOADED", FACEBOOK)
        }

        override fun onAdClicked(ad: Ad) {
            (mContext.applicationContext as StarsEarthApplication).analyticsManager.sendAnalyticsForAdvertisingEvent("AD_CLICKED", FACEBOOK)
        }

        override fun onLoggingImpression(ad: Ad) {
            (mContext.applicationContext as StarsEarthApplication).analyticsManager.sendAnalyticsForAdvertisingEvent("LOGGING_IMPRESSION", FACEBOOK)
        }
    }

    init {
        initializeGoogleAds(mContext)
        initializeFacebookAds(mContext)
        setupAdListeners()
    }

    fun shouldGenerateAd(context: Context?, teachingContent: SETeachingContent, results: List<Result>): Boolean {
        var ret = false
        val ads = (context as StarsEarthApplication).firebaseRemoteConfigWrapper.ads
        if (!ads.equals("None", ignoreCase = true) && context != null && teachingContent is Course) {
            val isAccessibilityUser = (context.applicationContext as? StarsEarthApplication)?.accessibilityManager?.isAccessibilityUser()
            val isOwnerWantingAds = teachingContent.isOwnerWantingAds
            val isAllowed = teachingContent.shouldGenerateAd(results)

            ret = isAccessibilityUser == false && isOwnerWantingAds && isAllowed
        } else if (!ads.equals("None", ignoreCase = true) && context != null && teachingContent is Task) {
            val isAccessibilityUser = (context.applicationContext as? StarsEarthApplication)?.accessibilityManager?.isAccessibilityUser()
            val isOwnerWantingAds = teachingContent.isOwnerWantingAds
            //For Task, isAllowed is decided by a coin toss
            val moduloString = context.firebaseRemoteConfigWrapper.adsFrequencyModulo
            val moduloInt = Integer.parseInt(moduloString)
            val random = Random()
            val isAllowed = moduloInt > 0 && random.nextInt(moduloInt) % moduloInt == 0

            ret = isAccessibilityUser == false && isOwnerWantingAds && isAllowed
        }
        return ret
    }

    fun shouldShowAd(teachingContent: SETeachingContent?, resultOfLastAttempt: Result) : Boolean {
        val isTeachingContentAllowingAd =
                if (teachingContent is Course) {
                    //If its a Course, only show if the task was passed
                    //mResults size should never be 0 here. It is called after saving a Result in onActivityResult
                    (teachingContent as Course).shouldShowAd(resultOfLastAttempt)
                }
                else if (teachingContent is Task) {
                    //If its a Task, no additional validation required
                    true
                }
                else {
                    //If its null, return false
                    false
                }
        return isTeachingContentAllowingAd
    }

    fun showAd() {
        val ads = (mContext as? StarsEarthApplication)?.firebaseRemoteConfigWrapper?.ads
      /*  if (ads.equals(GOOGLE, true) && googleInterstitialAd?.isLoaded == true) {
            googleInterstitialAd?.show()
        }
        else if (ads.equals(FACEBOOK, true) && facebookInterstitalAd?.isAdLoaded == true) {
            facebookInterstitalAd?.show()
        }   */
    }

    fun generateAd() {
        val ads = (mContext as StarsEarthApplication).firebaseRemoteConfigWrapper.ads
        if (ads.equals(GOOGLE, ignoreCase = true) /*&& googleInterstitialAd != null */&& adRequest != null) {
            //googleInterstitialAd!!.loadAd(adRequest!!.build())
        } else if (ads.equals(FACEBOOK, ignoreCase = true) && facebookInterstitalAd != null) {
            //AdSettings.addTestDevice("cc5a9eab-c86b-4529-83bb-902568670129"); //TS Mac simulator
            //AdSettings.addTestDevice("171f080c-a50d-457c-9226-bcdc194fda20"); //AH Mac simulator
            facebookInterstitalAd!!.loadAd() //This may need to be called on the UI thread
        }
    }

    fun generateAd(teachingContent: SETeachingContent?, results: List<Result>) {
        val shouldGenerate : Boolean? = teachingContent?.let { shouldGenerateAd(mContext, it, results) }
        if (shouldGenerate == true) {
            if (adRequest == null) {
                adRequest = AdRequest.Builder()
            }
            val tags = teachingContent.tags
            for (tag in tags) {
                adRequest!!.addKeyword(tag)
            }
            generateAd()
        }
    }

    private fun setupAdListeners() {
        //googleInterstitialAd!!.adListener = mGoogleAdListener
        facebookInterstitalAd!!.setAdListener(mFacebookAdListener)
    }

    private fun initializeGoogleAds(context: Context) {
        if (BuildConfig.DEBUG) {
            // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
            //MobileAds.initialize(context, "ca-app-pub-3940256099942544~3347511713")
            //googleInterstitialAd = InterstitialAd(context)
            //googleInterstitialAd!!.adUnitId = "ca-app-pub-3940256099942544/1033173712"
        } else {
            //MobileAds.initialize(context, "ca-app-pub-1378964097701084~9829207692")
            //googleInterstitialAd = InterstitialAd(context)
            //googleInterstitialAd!!.adUnitId = "ca-app-pub-1378964097701084/1268191394"
        }
    }

    private fun initializeFacebookAds(context: Context) {
        facebookInterstitalAd = com.facebook.ads.InterstitialAd(context, "2064355667218856_2069620790025677")
    }
}
