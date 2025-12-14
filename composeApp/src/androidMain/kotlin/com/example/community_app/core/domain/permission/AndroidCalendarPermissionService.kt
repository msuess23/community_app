package com.example.community_app.core.domain.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.community_app.core.util.ActivityProvider
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidCalendarPermissionService(
  private val context: Context
) : CalendarPermissionService {

  companion object {
    const val REQUEST_CODE = 101
    private var pendingContinuation: CancellableContinuation<PermissionStatus>? = null

    fun onPermissionResult(grantResults: IntArray) {
      val status = if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        PermissionStatus.GRANTED
      } else {
        PermissionStatus.DENIED
      }
      pendingContinuation?.resume(status)
      pendingContinuation = null
    }
  }

  private val permissions = arrayOf(
    Manifest.permission.READ_CALENDAR,
    Manifest.permission.WRITE_CALENDAR
  )

  override suspend fun checkPermission(): PermissionStatus {
    val read = ContextCompat.checkSelfPermission(context, permissions[0])
    val write = ContextCompat.checkSelfPermission(context, permissions[1])
    return if (read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED) {
      PermissionStatus.GRANTED
    } else {
      PermissionStatus.DENIED
    }
  }

  override suspend fun requestPermission(): PermissionStatus {
    if (checkPermission() == PermissionStatus.GRANTED) return PermissionStatus.GRANTED

    val activity = ActivityProvider.getActivity()
      ?: return PermissionStatus.DENIED

    return suspendCancellableCoroutine { cont ->
      pendingContinuation = cont
      ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE)
    }
  }

  override fun openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
      data = Uri.fromParts("package", context.packageName, null)
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
  }
}