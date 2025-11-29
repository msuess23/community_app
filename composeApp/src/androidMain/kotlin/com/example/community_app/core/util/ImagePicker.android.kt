package com.example.community_app.core.util

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

actual class ImagePickerFactory {
  @Composable
  actual fun createPicker(): ImagePicker = remember { ImagePicker() }
}

actual class ImagePicker {
  private var galleryLauncher: ActivityResultLauncher<String>? = null
  private var cameraLauncher: ActivityResultLauncher<Uri>? = null
  private var currentOnImagePicked: ((String) -> Unit)? = null
  private var context: Context? = null
  private var currentPhotoPath: String? = null

  @Composable
  actual fun registerPicker(onImagePicked: (String) -> Unit) {
    this.currentOnImagePicked = onImagePicked
    this.context = LocalContext.current

    galleryLauncher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
      uri?.let { processUri(it) }
    }

    cameraLauncher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.TakePicture()
    ) { success ->
      if (success && currentPhotoPath != null) {
        currentOnImagePicked?.invoke(currentPhotoPath!!)
      }
    }
  }

  private fun processUri(uri: Uri) {
    val ctx = context ?: return
    val tempFile = File(ctx.cacheDir, "picked_${UUID.randomUUID()}.jpg")
    ctx.contentResolver.openInputStream(uri)?.use { input ->
      tempFile.outputStream().use { output ->
        input.copyTo(output)
      }
    }
    currentOnImagePicked?.invoke(tempFile.absolutePath)
  }

  actual fun pickImage() {
    galleryLauncher?.launch("image/*")
  }

  actual fun takePhoto() {
    val ctx = context ?: return
    val file = File(ctx.cacheDir, "cam_${UUID.randomUUID()}.jpg")
    currentPhotoPath = file.absolutePath

    val authority = "${ctx.packageName}.provider"
    val uri = FileProvider.getUriForFile(ctx, authority, file)
    cameraLauncher?.launch(uri)
  }
}