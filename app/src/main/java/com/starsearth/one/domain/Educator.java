package com.starsearth.one.domain;

import java.util.Map;

public class Educator {

    public String uid;
    public String countryCode;
    public String phoneNumber;
    public Type type;

    public enum Type {
        AUTHORIZED, //Authorized to be an educator on the platform, but not registered
        ACTIVE, //Is currently an educator on the platform
        BLOCKED //Blocked for some wrong action
        ;

        public static Type fromString(String value) {
            Type result = null;
            switch (value.toLowerCase()) {
                case "authorized":
                    result = AUTHORIZED;
                    break;
                case "active":
                    result = ACTIVE;
                    break;
                case "blocked":
                    result = BLOCKED;
                    break;

                default: break;

            }

            return result;
        }
    };

    public Educator() {
        // Default constructor required for calls to DataSnapshot.getValueString(Educator.class)
    }

    public Educator(String key, Map<String, Object> map) {
        this.uid = key;
        this.countryCode = (String) map.get("countryCode");
        this.type = Type.fromString((String) map.get("type"));
        this.phoneNumber = (String) map.get("phoneNumber");
    }

    public Educator(String countryCode, String phoneNumber, Type type) {
        this.countryCode = countryCode;
        this.phoneNumber = phoneNumber;
        this.type = type;
    }
}
