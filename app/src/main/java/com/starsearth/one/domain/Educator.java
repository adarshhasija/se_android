package com.starsearth.one.domain;

public class Educator {

    public String countryCode;
    public String phoneNumber;
    public String uid;
    public Type type;

    public enum Type {
        AUTHORIZED(1), //Authorized to be an educator on the platform, but not registered
        ACTIVE(2), //Is currently an educator on the platform
        BLOCKED(3) //Blocked for some wrong action
        ;

        private final long value;

        Type(long value) {
            this.value = value;
        }

        public long getValue() {
            return value;
        }

        @Override
        public String toString() {
            String result = null;
            switch ((int) value) {
                case 1:
                    result = "Authorized";
                    break;
                case 2:
                    result = "Active";
                    break;
                case 3:
                    result = "Blocked";
                    break;

                default: break;

            }

            return result;
        }

        public static Type fromInt(long i) {
            for (Type type : Type.values()) {
                if (type.getValue() == i) { return type; }
            }
            return null;
        }
    };

    public Educator() {
        // Default constructor required for calls to DataSnapshot.getValueString(Educator.class)
    }

    public Educator(String countryCode, String phoneNumber, Type type) {
        this.countryCode = countryCode;
        this.phoneNumber = phoneNumber;
        this.type = type;
    }
}
