package com.example.community_app.core.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.darwin.NSObject
import platform.posix.memcpy

actual class ImagePickerFactory {
  @Composable
  actual fun createPicker(): ImagePicker {
    return remember { ImagePicker() }
  }
}

actual class ImagePicker {
  private var onImagePicked: ((ByteArray) -> Unit)? = null
  private val delegate = ImagePickerDelegate { data ->
    onImagePicked?.invoke(data)
  }

  @Composable
  actual fun registerPicker(onImagePicked: (ByteArray) -> Unit) {
    this.onImagePicked = onImagePicked
  }

  actual fun pickImage() {
    showPicker(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary)
  }

  actual fun takePhoto() {
    showPicker(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)
  }

  private fun showPicker(sourceType: UIImagePickerControllerSourceType) {
    val picker = UIImagePickerController()
    picker.sourceType = sourceType
    picker.delegate = delegate

    val rootController = UIApplication.sharedApplication.keyWindow?.rootViewController
    rootController?.presentViewController(picker, animated = true, completion = null)
  }
}

class ImagePickerDelegate(
  private val onImagePicked: (ByteArray) -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

  @OptIn(ExperimentalForeignApi::class)
  override fun imagePickerController(
    picker: UIImagePickerController,
    didFinishPickingMediaWithInfo: Map<Any?, *>
  ) {
    val image = didFinishPickingMediaWithInfo["UIImagePickerControllerOriginalImage"] as? UIImage
    image?.let {
      val jpegData = UIImageJPEGRepresentation(it, 0.8)
      jpegData?.let { data ->
        if (data.length > 0u) {
          data.bytes?.let { ptr ->
            val bytes = toByteArray(ptr, data.length.toInt())
            onImagePicked(bytes)
          }
        }
      }
    }
    picker.dismissViewControllerAnimated(true, completion = null)
  }

  @OptIn(ExperimentalForeignApi::class)
  private fun toByteArray(ptr: CPointer<*>, length: Int): ByteArray {
    return ByteArray(length).apply {
      usePinned { pinned ->
        memcpy(pinned.addressOf(0), ptr, length.toULong())
      }
    }
  }

  override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
    picker.dismissViewControllerAnimated(true, completion = null)
  }
}