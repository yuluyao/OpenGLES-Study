package com.vegeta.glstudy

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLUtils
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

object Qutil {
  private val TAG = "glgl"

  fun initShader(context: Context, vsResId: Int, fsResId: Int): Int {
    val vertexShader: Int = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER).also { shader ->
      GLES30.glShaderSource(shader, loadShaderFile(context, vsResId))
      GLES30.glCompileShader(shader)
    }
    val fragmentShader: Int = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER).also { shader ->
      GLES30.glShaderSource(shader, loadShaderFile(context, fsResId))
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

   private fun loadShaderFile(context: Context, resId: Int): String {
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

  fun loadTexture(context: Context): IntArray {
    val result = IntArray(3)
    val textureIds = IntArray(1)
    GLES30.glGenTextures(1, textureIds, 0)
    if (textureIds[0] == 0) {
      Log.e(TAG, "create texture object failed!")
      return result
    }
    result[0]= textureIds[0]

    val options = BitmapFactory.Options().apply {
      inScaled = false
    }
    val bitmap =
      BitmapFactory.decodeResource(context.resources, R.drawable.test_texture, options)
        ?: run {
          GLES30.glDeleteTextures(1, textureIds, 0)
          Log.e(TAG, "加载bitmap错误")
          return result
        }
    // 绑定纹理
    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureIds[0])
    // 设置纹理过滤参数
    GLES30.glTexParameteri(
      GLES30.GL_TEXTURE_2D,
      GLES30.GL_TEXTURE_MIN_FILTER,
      GLES30.GL_LINEAR_MIPMAP_LINEAR
    )
    GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
    // 加载bitmap到纹理
    GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
    // 生成mipmap
    GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)

    result[1] = bitmap.width
    result[2] = bitmap.height
    // 释放Bitmap
    bitmap.recycle()
    // 解绑
    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)

    return result
  }

}