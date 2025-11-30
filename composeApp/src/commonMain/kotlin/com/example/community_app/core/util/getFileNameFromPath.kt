package com.example.community_app.core.util

fun getFileNameFromPath(path: String): String {
  val i = path.lastIndexOf('/')
  return if (i >= 0) path.substring(i + 1) else path
}