package com.starsearth.one.managers

import android.app.Activity
import android.content.Context
import android.os.Bundle

import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsConstants
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.analytics.FirebaseAnalytics
import com.starsearth.one.BuildConfig
import com.starsearth.one.R
import com.starsearth.one.application.StarsEarthApplication
import com.starsearth.one.domain.*

class AnalyticsManager(private val mContext: Context) {

    private var firebaseAnalytics: FirebaseAnalytics? = null
    private var facebookAnalytics: AppEventsLogger? = null

    companion object {
        enum class GESTURES {
            TAP, LONG_PRESS, SWIPE
        }
    }


    init {
        if (!BuildConfig.DEBUG) {
            val remoteConfigAnalytics = (mContext as StarsEarthApplication).remoteConfigAnalytics
            if (remoteConfigAnalytics.equals("all", ignoreCase = true)) {
                initializeFirebaseAnalytics()
                initializeFacebookAnalytics()
            } else if (remoteConfigAnalytics.equals("firebase", ignoreCase = true)) {
                initializeFirebaseAnalytics()
            } else if (remoteConfigAnalytics.equals("facebook", ignoreCase = true)) {
                initializeFacebookAnalytics()
            }
        }
    }

    private fun initializeFirebaseAnalytics() {
        firebaseAnalytics = FirebaseAnalytics.getInstance(mContext)
    }

    private fun initializeFacebookAnalytics() {
        FacebookSdk.setApplicationId(mContext.resources.getString(R.string.facebook_app_id))
        FacebookSdk.sdkInitialize(mContext)
        AppEventsLogger.activateApp(mContext)
        facebookAnalytics = AppEventsLogger.newLogger(mContext)
    }

    fun logActionEvent(eventName: String, bundle: Bundle) {
        if (firebaseAnalytics != null) {
            firebaseAnalytics!!.logEvent(eventName, bundle)
        }
        if (facebookAnalytics != null) {
            facebookAnalytics!!.logEvent(eventName, bundle)
        }
    }

    fun logActionEvent(eventName: String, bundle: Bundle, score: Int) {
        if (firebaseAnalytics != null) {
            firebaseAnalytics!!.logEvent(eventName, bundle)
        }
        if (facebookAnalytics != null) {
            facebookAnalytics!!.logEvent(eventName, score.toDouble(), bundle)
        }
    }

    fun logFragmentViewEvent(fragmentName: String, activity: Activity) {
        if (firebaseAnalytics != null) {
            firebaseAnalytics!!.setCurrentScreen(activity, fragmentName, null)
        }
        if (facebookAnalytics != null) {
            val bundle = Bundle()
            bundle.putString("content", fragmentName)
            facebookAnalytics!!.logEvent(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, bundle)
        }
    }

    fun updateUserAnalyticsInfo(userId: String) {
        updateAnalyticsUserId(userId)
        updateUserProperties()
    }

    fun updateAnalyticsUserId(userId: String) {
        if (firebaseAnalytics != null) {
            firebaseAnalytics!!.setUserId(userId)
        }
        if (facebookAnalytics != null) {
            AppEventsLogger.setUserID(userId)
        }
    }

    private fun updateUserProperties() {
        val accessibility = (mContext as StarsEarthApplication).accessibility
        val user_props = accessibility.userPropertiesAccessibility
        if (firebaseAnalytics != null && user_props != null) {
            for (key in user_props.keySet()) {
                firebaseAnalytics!!.setUserProperty(key, user_props.get(key)!!.toString()) //must be a string
            }
        }
        if (facebookAnalytics != null) {
            AppEventsLogger.updateUserProperties(user_props, null)
        }
    }



    /*
        Analytics event for detail screen gesture
     */
    fun sendAnalyticsForDetailScreenGesture(teachingContent: Any?, action: String) {
        val bundle = Bundle()
        bundle.putString("action", action)
        bundle.putString("title", (teachingContent as? SEBaseObject)?.title)
        bundle.putString("type", if (teachingContent is Course) {
            "course"
        } else {
            "task"
        })
        logActionEvent("se1_detail_screen_gesture", bundle)
    }

    /*
    Analytics event to know if the user exited the login process
    This will only be sent during the login process. Not during the edit phone number process
    @params: title: to know which was selected.
                task: to know task for which detail item was selected
               index: to know if the user had to scroll down to find item
     */
    fun sendAnalyticsForLoginPhoneNumberExit(screen: String, action: String) {
        val bundle = Bundle()
        bundle.putString("screen", screen)
        bundle.putString("action", action)
        logActionEvent("se1_exited_login", bundle)
    }

    /*
    Analytics event for successful signin with phone number
    This will only be sent during the signin phone number process.
    @params: screen: the screen we came from. Edit phone number or verify OTP. This is to know if the system read the OTP automatically

     */
    fun sendAnalyticsForLoginWithPhoneNumber(screen: String) {
        val bundle = Bundle()
        bundle.putString("screen", screen)
        logActionEvent("se1_phone_number_updated", bundle)
    }

    /*
    Analytics event for successful updating of phone number
    This will only be sent during the edit phone number process.
    @params: screen: the screen we came from. Edit phone number or verify OTP. This is to know if the system read the OTP automatically

     */
    fun sendAnalyticsForUpdatedPhoneNumber(screen: String) {
        val bundle = Bundle()
        bundle.putString("screen", screen)
        logActionEvent("se1_phone_number_updated", bundle)
    }

    /*
    Analytics event for detail item tap
    @params: title: to know which was selected.
                task: to know task for which detail item was selected
               index: to know if the user had to scroll down to find item
     */
    fun sendAnalyticsForDetailListItemTap(title: String, teachingContent: SEBaseObject?) {
        val bundle = Bundle()
        bundle.putString("title", title)
        bundle.putString("task_title", teachingContent?.title)
        logActionEvent("se1_detail_list_item_tap", bundle)
    }

    /*
    Analytics event for detail item long press
    @params: title: to know which was selected.
                task: to know task for which detail item was selected
               index: to know if the user had to scroll down to find item
     */
    fun sendAnalyticsForDetailListItemLongPress(title: String, teachingContent: SEBaseObject?) {
        val bundle = Bundle()
        bundle.putString("title", title)
        bundle.putString("task_title", teachingContent?.title)
        logActionEvent("se1_detail_list_item_long_press", bundle)
    }

    /*
        Results screen gesture to open responses
     */
    fun sendAnalyticsForResultsScreenGesture(task: Task?, action: String) {
        val bundle = Bundle()
        bundle.putString("action", action)
        bundle.putString("title", task?.title)
        bundle.putString("type", "task")
        logActionEvent("se1_results_screen_gesture", bundle)
    }

    /*
    Analytics event for list item tap
    @params: selected: to know which was selected.
               index: to know if the user had to scroll down to find item
     */
    fun sendAnalyticsForListItemTap(selected: String, index: Int) {
        val bundle = Bundle()
        bundle.putString("title", selected)
        bundle.putInt("index", index)
        logActionEvent("se1_list_item_tap", bundle)
    }

    /*
    Analytics event for record list item tap
    @params: selected: to know which was selected.
               index: to know if the user had to scroll down to find item
     */
    fun sendAnalyticsForRecordListItemTap(selected: RecordItem, index: Int) {
        val bundle = Bundle()
        bundle.putString("title", (selected.teachingContent as? SEBaseObject)?.title)
        bundle.putInt("index", index)
        logActionEvent("se1_record_list_item_tap", bundle)
    }

    /*
    Analytics event for result list item tap
    @params: task: to know the nature of the task for which results were viewed.
     */
    fun sendAnalyticsForResultListItemTap(parentTask: Task?) {
        val bundle = Bundle()
        bundle.putString("task_title", parentTask?.title)
        bundle.putString("task_type", parentTask?.type.toString())
        bundle.putBoolean("task_timed", parentTask?.timed ?: false)
        bundle.putBoolean("task_isGame", parentTask?.isGame ?: false)
        logActionEvent("se1_result_list_item_tap", bundle)
    }

    /*
    Analytics event for task cancellation
    @params: task: to know the nature of the task for which results were viewed.
            reason: reason for cancellation
     */
    fun sendAnalyticsForTaskCancellation(task: Task?, reason: String) {
        val bundle = Bundle()
        bundle.putString("reason", reason)
        bundle.putString("task_title", task?.title)
        bundle.putString("task_type", task?.type.toString())
        bundle.putBoolean("task_timed", task?.timed ?: false)
        bundle.putBoolean("task_isGame", task?.isGame ?: false)
        logActionEvent("se1_task_cancelled", bundle)
    }


    fun sendAnalyticsForAdvertisingEvent(callbackType: String, sourcePlatform: String) {
        val bundle = Bundle()
        bundle.putString("callback_type", callbackType)
        bundle.putString("source_platform", sourcePlatform)
        logActionEvent("se1_advertisement_event", bundle)
    }


}
