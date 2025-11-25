package com.example.community_app.core.util

import android.app.Activity
import java.lang.ref.WeakReference

object AndroidAppRestarter {
  private var currentActivity: WeakReference<Activity>? = null

  fun setActivity(activity: Activity) {
    currentActivity = WeakReference(activity)
  }

  fun restart() {
    currentActivity?.get()?.recreate()
  }
}

actual fun restartApp() {
  AndroidAppRestarter.restart()
}