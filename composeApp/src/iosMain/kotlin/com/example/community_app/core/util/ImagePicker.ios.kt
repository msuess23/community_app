package com.example.community_app.core.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.writeToFile
import platform.Foundation.writeToURL
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.darwin.NSObject

actual class ImagePickerFactory {
  @Composable
  actual fun createPicker(): ImagePicker = remember { ImagePicker() }
}

actual class ImagePicker {
  private var onImagePicked: ((String) -> Unit)? = null
  private val delegate = ImagePickerDelegate { path ->
    onImagePicked?.invoke(path)
  }

  @Composable
  actual fun registerPicker(onImagePicked: (String) -> Unit) {
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
    UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
      viewControllerToPresent = picker,
      animated = true,
      completion = null
    )
  }
}

class ImagePickerDelegate(
  private val onImageSaved: (String) -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

  @OptIn(ExperimentalForeignApi::class)
  override fun imagePickerController(
    picker: UIImagePickerController,
    didFinishPickingMediaWithInfo: Map<Any?, *>
  ) {
    val image = didFinishPickingMediaWithInfo["UIImagePickerControllerOriginalImage"] as? UIImage

    image?.let { uiImage ->
      val jpegData = UIImageJPEGRepresentation(uiImage, 0.8)
      jpegData?.let { data ->
        val fileName = "temp_${NSUUID().UUIDString}.jpg"
        val tempDir = NSTemporaryDirectory()
        val fullPath = tempDir + fileName
        val url = NSURL.fileURLWithPath(fullPath)

        data.writeToURL(url, true)
        onImageSaved(fullPath)
      }
    }
    picker.dismissViewControllerAnimated(true, completion = null)
  }

  override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
    picker.dismissViewControllerAnimated(true, completion = null)
  }
}