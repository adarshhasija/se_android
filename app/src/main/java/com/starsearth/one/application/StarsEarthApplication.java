package com.starsearth.one.application;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Build;
import android.support.v7.app.AlertDialog;

import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.starsearth.one.R;
import com.starsearth.one.domain.SeOneAccessibilityManager;
import com.starsearth.one.managers.AdsManager;
import com.starsearth.one.managers.AnalyticsManager;
import com.starsearth.one.domain.FirebaseRemoteConfigWrapper;
import com.starsearth.one.domain.User;

import java.io.ByteArrayOutputStream;

/**
 * Created by faimac on 11/28/16.
 */

public class StarsEarthApplication extends Application {

    private FirebaseRemoteConfigWrapper mFirebaseRemoteConfigWrapper;
    private AnalyticsManager mAnalyticsManager;
    private SeOneAccessibilityManager mSeOneAccessibilityManager;
    private AdsManager mAdsManager;

    private User user;

    public FirebaseRemoteConfigWrapper getFirebaseRemoteConfigWrapper() {
        return mFirebaseRemoteConfigWrapper;
    }

    public InterstitialAd getGoogleInterstitialAd() {
        InterstitialAd result = null;
        if (mAdsManager != null) {
            result = mAdsManager.getGoogleInterstitialAd();
        }
        return result;
    }

    public com.facebook.ads.InterstitialAd getFacebookInterstitalAd() {
        com.facebook.ads.InterstitialAd result = null;
        if (mAdsManager != null) {
            result = mAdsManager.getFacebookInterstitalAd();
        }
        return result;
    }

    public AdsManager getAdsManager() {
        return mAdsManager;
    }

    public AnalyticsManager getAnalyticsManager() {
        return mAnalyticsManager;
    }

    public SeOneAccessibilityManager getAccessibilityManager() {
        return mSeOneAccessibilityManager;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true); //This causes bugs. Data from server does not update on client

        mFirebaseRemoteConfigWrapper = new FirebaseRemoteConfigWrapper(getApplicationContext());
        mAnalyticsManager = new AnalyticsManager(getApplicationContext());
        mSeOneAccessibilityManager = new SeOneAccessibilityManager(getApplicationContext());
        mAdsManager = new AdsManager(getApplicationContext());

        //Skill skill = new Skill("Adarsh", "Hasija", "sample_email@gmail.com", "accessibility6");
        //String key = mDatabase.push().getKey();
        //mDatabase.child("skills").child(key).setValue(skill);

        //DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        //Educator educator = new Educator("+91", "+918050389798", Educator.Status.AUTHORIZED);
        //String keyEducator = mDatabase.push().getKey();
        //mDatabase.child("educators").child(keyEducator).setValue(educator);
        //mDatabase.child("educators").child("-LuEFLKyquYUkh64ZGR-").child("status").setValue(Educator.Status.AUTHORIZED);
        //mDatabase.child("users").child("RycAhfhPsXOrUdbO8GOJqucktAA3").child("name").setValue("Adarsh Hasija");
        //StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images/RycAhfhPsXOrUdbO8GOJqucktAA3.jpg");
        //Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.profilepic);
        //ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        //byte[] data = baos.toByteArray();

        //UploadTask uploadTask = storageRef.putBytes(data);
     /*   uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                mDatabase.child("users").child("RycAhfhPsXOrUdbO8GOJqucktAA3").child("pic").setValue("images/RycAhfhPsXOrUdbO8GOJqucktAA3.jpg");
            }
        }); */


        //TagListItem tag = new TagListItem("Class 3", true);
        //String key = mDatabase.push().getKey();
        //mDatabase.child("tags").child(key).setValue(tag);
    }

    public String getRemoteConfigAnalytics() {
        return mFirebaseRemoteConfigWrapper.get("analytics");
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

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

}
