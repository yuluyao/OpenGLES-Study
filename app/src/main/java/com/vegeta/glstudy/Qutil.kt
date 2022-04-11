package com.vegeta.glstudy

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

object Qutil {
  private val TAG = "glgl"

  fun loadShaderFile(context: Context, resId: Int): String {
    val inputStream = context.resources.openRawResource(resId)
    val reader = InputStreamReader(inputStream)
    val bufferedReader = BufferedReader(reader)
    val buffer = StringBuffer("")
    var str: String?
    while (bufferedReader.readLine().also { str = it } != null) {
      buffer.append(str)
      Log.i(TAG, "[read]: $str")
      buffer.append("\n")
    }
    return buffer.toString()
  }

}