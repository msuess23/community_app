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

actual class ImagePickerFactory {
  @Composable
  actual fun createPicker(): ImagePicker {
    return remember { ImagePicker() }
  }
}

actual class ImagePicker {
  private var galleryLauncher: ActivityResultLauncher<String>? = null
  private var cameraLauncher: ActivityResultLauncher<Uri>? = null

  private var currentOnImagePicked: ((ByteArray) -> Unit)? = null
  private var context: Context? = null

  private var currentPhotoUri: Uri? = null

  @Composable
  actual fun registerPicker(onImagePicked: (ByteArray) -> Unit) {
    this.currentOnImagePicked = onImagePicked
    this.context = LocalContext.current

    galleryLauncher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
      uri?.let {
        val bytes = context?.contentResolver?.openInputStream(it)?.use { stream ->
          stream.readBytes()
        }
        bytes?.let { b -> onImagePicked(b) }
      }
    }

    cameraLauncher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.TakePicture()
    ) { success ->
      if (success && currentPhotoUri != null) {
        try {
          val bytes = context?.contentResolver?.openInputStream(currentPhotoUri!!)?.use { stream ->
            stream.readBytes()
          }
          bytes?.let { b -> onImagePicked(b) }
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }

  actual fun pickImage() {
    galleryLauncher?.launch("image/*")
  }

  actual fun takePhoto() {
    val ctx = context ?: return

    val file = File.createTempFile("img_", ".jpg", ctx.cacheDir)

    val authority = "${ctx.packageName}.provider"
    val uri = FileProvider.getUriForFile(ctx, authority, file)

    currentPhotoUri = uri
    cameraLauncher?.launch(uri)
  }
}