package com.vegeta.glstudy

import android.content.Context
import android.opengl.GLES30
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

object Qutil {
  private val TAG = "glgl"

  fun initShader(context: Context,vsResId:Int,fsResId:Int):Int {
    val vertexShader: Int = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER).also { shader ->
      GLES30.glShaderSource(shader, loadShaderFile(context,vsResId))
      GLES30.glCompileShader(shader)
    }
    val fragmentShader: Int = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER).also { shader ->
      GLES30.glShaderSource(shader, loadShaderFile(context,fsResId))
      GLES30.glCompileShader(shader)
    }
    val glProgram = GLES30.glCreateProgram().also {
      GLES30.glAttachShader(it, vertexShader)
      GLES30.glAttachShader(it, fragmentShader)
      GLES30.glLinkProgram(it)
    }
    GLES30.glUseProgram(glProgram)
    return glProgram
  }

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