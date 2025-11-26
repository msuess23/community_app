package com.example.community_app.core.domain.location

import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidLocationService(
  private val context: Context
) : LocationService {
  private val client = LocationServices.getFusedLocationProviderClient(context)

  override suspend fun getCurrentLocation(): Location? {
    val hasFine = ContextCompat.checkSelfPermission(
      context, android.Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val hasCoarse = ContextCompat.checkSelfPermission(
      context, android.Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    if (!hasCoarse && !hasFine || !isGpsEnabled) return null

    return suspendCancellableCoroutine { cont ->
      client.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
          cont.resume(Location(location.latitude, location.longitude))
        } else {
          requestFreshLocation(cont)
        }
      }.addOnFailureListener {
        requestFreshLocation(cont)
      }
    }
  }

  private fun requestFreshLocation(cont: CancellableContinuation<Location?>) {
    val cancellationTokenSource = CancellationTokenSource()

    client.getCurrentLocation(
      Priority.PRIORITY_HIGH_ACCURACY,
      cancellationTokenSource.token
    ).addOnSuccessListener { location ->
      if (location != null) {
        cont.resume(Location(location.latitude, location.longitude))
      } else {
        cont.resume(null)
      }
    }.addOnFailureListener {
      cont.resume(null)
    }

    cont.invokeOnCancellation {
      cancellationTokenSource.cancel()
    }
  }
}