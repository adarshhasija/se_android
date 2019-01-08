package com.starsearth.one.domain;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.view.accessibility.AccessibilityManager;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.ACCESSIBILITY_SERVICE;

public class SeOneAccessibilityManager {

    private Context mContext;

    public SeOneAccessibilityManager(Context context) {
        mContext = context;
    }

    public Bundle getUserPropertiesAccessibility() {
        Bundle bundle = new Bundle();
        bundle.putInt("talkback", isTalkbackOn()? 1 : 0);
        bundle.putInt("magnification", isMagnificationServiceOn()? 1 : 0);
        bundle.putInt("select_to_speak", isSelectToSpeakOn()? 1 : 0);
        bundle.putInt("switch_access", isSwitchAccessOn()? 1 : 0);
        bundle.putInt("voice_access", isVoiceAccessOn()? 1 : 0);
        bundle.putInt("braille_back", isBrailleBackOn()? 1 : 0);
        return bundle;
    }

    public List<String> getAccessibilityEnabledServiceNames() {
        List<String> result = new ArrayList<>();
        AccessibilityManager am = (AccessibilityManager) mContext.getSystemService(ACCESSIBILITY_SERVICE);
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

    public boolean isAccessibilityUser() {
        boolean result = false;
        List<String> names = getAccessibilityEnabledServiceNames();
        if (names.size() > 0) {
            result = true;
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
    }
}
