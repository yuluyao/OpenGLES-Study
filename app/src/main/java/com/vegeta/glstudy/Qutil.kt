package com.vegeta.glstudy

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLES30.*
import android.opengl.GLUtils
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

object Qutil {
  private val TAG = "glgl"

  fun initShader(context: Context, vsResId: Int, fsResId: Int): Int {
    val vertexShader: Int = glCreateShader(GL_VERTEX_SHADER).also { shader ->
      glShaderSource(shader, loadShaderFile(context, vsResId))
      glCompileShader(shader)
    }
    val fragmentShader: Int = glCreateShader(GL_FRAGMENT_SHADER).also { shader ->
      glShaderSource(shader, loadShaderFile(context, fsResId))
      glCompileShader(shader)
    }
    val glProgram = glCreateProgram().also {
      glAttachShader(it, vertexShader)
      glAttachShader(it, fragmentShader)
      glLinkProgram(it)
    }
    glUseProgram(glProgram)
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
      Log.v(TAG, "[read]: $str")
      buffer.append("\n")
    }
    return buffer.toString()
  }

  fun loadTexture(context: Context, resId: Int): IntArray {
    val result = IntArray(3)
    val textureIds = IntArray(1)
    glGenTextures(1, textureIds, 0)
    if (textureIds[0] == 0) {
      Log.e(TAG, "create texture object failed!")
      return result
    }
    result[0] = textureIds[0]

    val options = BitmapFactory.Options().apply {
      inScaled = false
      inSampleSize = 2

    }
    val bitmap =
      BitmapFactory.decodeResource(context.resources, resId, options)
        ?: run {
          glDeleteTextures(1, textureIds, 0)
          Log.e(TAG, "加载bitmap错误")
          return result
        }
    // 绑定纹理
    glBindTexture(GL_TEXTURE_2D, textureIds[0])
    // 设置纹理过滤参数
    glTexParameteri(
      GL_TEXTURE_2D,
      GL_TEXTURE_MIN_FILTER,
      GL_LINEAR_MIPMAP_LINEAR
    )
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
    // 加载bitmap到纹理
    GLUtils.texImage2D(GL_TEXTURE_2D, 0, GL_RGBA, bitmap, 0)
    // 生成mipmap
    glGenerateMipmap(GL_TEXTURE_2D)

    result[1] = bitmap.width
    result[2] = bitmap.height
    Log.i(
      TAG,
      "bitmap: [memory: ${bitmap.byteCount}, width: ${bitmap.width}, height: ${bitmap.height}]"
    )
    // 释放Bitmap
    bitmap.recycle()
    // 解绑
    glBindTexture(GL_TEXTURE_2D, 0)

    return result
  }

  fun array2Buffer(array: ShortArray): ShortBuffer {
    val bb = ByteBuffer.allocateDirect(array.size * Short.SIZE_BYTES)
    bb.order(ByteOrder.nativeOrder())
    val buffer = bb.asShortBuffer()
    buffer.put(array)
    buffer.position(0)
    return buffer
  }

  fun array2Buffer(array: FloatArray): FloatBuffer {
    val bb = ByteBuffer.allocateDirect(array.size * Float.SIZE_BYTES)
    bb.order(ByteOrder.nativeOrder())
    val buffer = bb.asFloatBuffer()
    buffer.put(array)
    buffer.position(0)
    return buffer
  }
}