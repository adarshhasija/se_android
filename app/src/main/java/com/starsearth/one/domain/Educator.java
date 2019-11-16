package com.starsearth.one.domain;

import java.util.Map;

public class Educator {

    //WARNING: Do not use camel case or underscore for variable names. Leads to bugs on server side. Use abbreviations and explain the meaning in comments

    public String uid;
    public String cc; //Country code
    public String mpn; //Mobile phone number
    public Status status;

    public enum Status {
        AUTHORIZED, //Authorized to be an educator on the platform, but not registered
        ACTIVE, //Is currently an educator on the platform
        SUSPENDED, //Suspended for some wrong action
        DEACTIVATED
        ;

        public static Status fromString(String value) {
            Status result = null;
            switch (value.toLowerCase()) {
                case "authorized":
                    result = AUTHORIZED;
                    break;
                case "active":
                    result = ACTIVE;
                    break;
                case "suspended":
                    result = SUSPENDED;
                    break;
                case "deactivated":
                    result = DEACTIVATED;
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
        this.cc = (String) map.get("cc");
        this.status = Status.fromString((String) map.get("status"));
        this.mpn = (String) map.get("mpn");
    }

    public Educator(String cc, String mpn, Status status) {
        this.cc = cc;
        this.mpn = mpn;
        this.status = status;
    }
}
