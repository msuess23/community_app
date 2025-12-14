package com.example.community_app.core.util

import android.app.Activity
import java.lang.ref.WeakReference

object ActivityProvider {
  private var currentActivity: WeakReference<Activity>? = null

  fun setActivity(activity: Activity) {
    currentActivity = WeakReference(activity)
  }

  fun getActivity(): Activity? = currentActivity?.get()
}