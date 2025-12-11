package com.example.community_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.community_app.app.App
import com.example.community_app.core.domain.permission.AndroidCalendarPermissionService
import com.example.community_app.core.util.ActivityProvider
import com.example.community_app.core.util.AndroidAppRestarter

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)

    AndroidAppRestarter.setActivity(this)
    ActivityProvider.setActivity(this)

    setContent {
      App()
    }
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String?>,
    grantResults: IntArray,
    deviceId: Int
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)

    if (requestCode == 101) {
      AndroidCalendarPermissionService.onPermissionResult(grantResults)
    }
  }
}

@Preview
@Composable
fun AppAndroidPreview() {
  App()
}