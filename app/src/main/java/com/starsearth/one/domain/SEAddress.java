package com.starsearth.one.domain;

import android.location.Address;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

public class SEAddress implements Parcelable {

    public String addressLine;
    public String locality; //city
    public String adminArea; //state
    public String countryName;
    public String postalCode;
    public String featureName;
    public String subLocality;
    public String premesis;
    public String subAdminArea;
    public Double latitude;
    public Double longitude;

    public SEAddress(Address address) {
        this.addressLine = address.getAddressLine(0);
        this.locality = address.getLocality();
        this.adminArea = address.getAdminArea();
        this.countryName = address.getCountryName();
        this.postalCode = address.getPostalCode();
        this.featureName = address.getFeatureName();
        this.subLocality = address.getSubLocality();
        this.premesis = address.getPremises();
        this.subAdminArea = address.getSubAdminArea();
        this.latitude = address.getLatitude();
        this.longitude = address.getLongitude();
    }

    public SEAddress(HashMap<String, Object> map) {
        this.addressLine = map.containsKey("addressLine") ? (String) map.get("addressLine") : null;
        this.locality = map.containsKey("locality") ? (String) map.get("locality") : null;
        this.adminArea = map.containsKey("adminArea") ? (String) map.get("adminArea") : null;
        this.countryName = map.containsKey("countryName") ? (String) map.get("countryName") : null;
        this.postalCode = map.containsKey("postalCode") ? (String) map.get("postalCode") : null;
        this.featureName = map.containsKey("featureName") ? (String) map.get("featureName") : null;
        this.subLocality = map.containsKey("subLocality") ? (String) map.get("subLocality") : null;
        this.premesis = map.containsKey("premesis") ? (String) map.get("premesis") : null;
        this.subAdminArea = map.containsKey("subAdminArea") ? (String) map.get("subAdminArea") : null;
        this.latitude = map.containsKey("latitude") ? (Double) map.get("latitude") : 0;
        this.longitude = map.containsKey("longitude") ? (Double) map.get("longitude") : 0;
    }


    protected SEAddress(Parcel in) {
        addressLine = in.readString();
        locality = in.readString();
        adminArea = in.readString();
        countryName = in.readString();
        postalCode = in.readString();
        featureName = in.readString();
        subLocality = in.readString();
        premesis = in.readString();
        subAdminArea = in.readString();
        if (in.readByte() == 0) {
            latitude = null;
        } else {
            latitude = in.readDouble();
        }
        if (in.readByte() == 0) {
            longitude = null;
        } else {
            longitude = in.readDouble();
        }
    }

    public static final Creator<SEAddress> CREATOR = new Creator<SEAddress>() {
        @Override
        public SEAddress createFromParcel(Parcel in) {
            return new SEAddress(in);
        }

        @Override
        public SEAddress[] newArray(int size) {
            return new SEAddress[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(addressLine);
        parcel.writeString(locality);
        parcel.writeString(adminArea);
        parcel.writeString(countryName);
        parcel.writeString(postalCode);
        parcel.writeString(featureName);
        parcel.writeString(subLocality);
        parcel.writeString(premesis);
        parcel.writeString(subAdminArea);
        if (latitude == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(latitude);
        }
        if (longitude == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(longitude);
        }
    }
}
