package com.example.community_app

import android.app.Application
import com.example.community_app.di.initKoin
import org.koin.android.ext.koin.androidContext

class CommunityApplication: Application() {
  override fun onCreate() {
    super.onCreate()
    initKoin {
      androidContext(this@CommunityApplication)
    }
  }
}