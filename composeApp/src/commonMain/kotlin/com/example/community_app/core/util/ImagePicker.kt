package com.example.community_app.core.util

import androidx.compose.runtime.Composable

expect class ImagePickerFactory() {
  @Composable
  fun createPicker(): ImagePicker
}

expect class ImagePicker {
  @Composable
  fun registerPicker(onImagePicked: (String) -> Unit)

  fun pickImage()
  fun takePhoto()
}