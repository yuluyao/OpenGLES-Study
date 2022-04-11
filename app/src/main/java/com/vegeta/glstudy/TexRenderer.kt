package com.vegeta.glstudy

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class TexRenderer(val context: Context) : GLSurfaceView.Renderer {

  val mvpMatrix = FloatArray(16).apply { Matrix.setIdentityM(this, 0) }
  val projMatrix = FloatArray(16).apply { Matrix.setIdentityM(this, 0) }
  val viewMatrix = FloatArray(16).apply { Matrix.setIdentityM(this, 0) }
  val modelMatrix = FloatArray(16).apply { Matrix.setIdentityM(this, 0) }

  /**
   * 顶点坐标
   */
  var vertices = floatArrayOf(
    0f, 0f, 0f,     //顶点坐标V0
    1f, 1f, 0f,     //顶点坐标V1
    -1f, 1f, 0f,    //顶点坐标V2
    -1f, -1f, 0f,   //顶点坐标V3
    1f, -1f, 0f     //顶点坐标V4
  )

  private var vertexBuffer = ByteBuffer.allocateDirect(vertices.size * Float.SIZE_BYTES).run {
    order(ByteOrder.nativeOrder())
    asFloatBuffer().apply {
      put(vertices)
      position(0)
    }
  }

  /**
   * 顶点索引
   */
  var vertexIndex = intArrayOf(
    0, 1, 2,  //V0,V1,V2 三个顶点组成一个三角形
    0, 2, 3,  //V0,V2,V3 三个顶点组成一个三角形
    0, 3, 4,  //V0,V3,V4 三个顶点组成一个三角形
    0, 4, 1   //V0,V4,V1 三个顶点组成一个三角形
  )

  private val vertexIndexBuffer = ByteBuffer.allocateDirect(vertexIndex.size * Int.SIZE_BYTES).run {
    order(ByteOrder.nativeOrder())
    asIntBuffer().apply {
      put(vertexIndex)
      position(0)
    }
  }

  /**
   * 纹理坐标
   */
  var texVertices = floatArrayOf(
    0.5f, 0.5f, //纹理坐标V0
    1f, 0f,     //纹理坐标V1
    0f, 0f,     //纹理坐标V2
    0f, 1.0f,   //纹理坐标V3
    1f, 1.0f    //纹理坐标V4
  )

  private var texBuffer = ByteBuffer.allocateDirect(texVertices.size * Float.SIZE_BYTES).run {
    order(ByteOrder.nativeOrder())
    asFloatBuffer().apply {
      put(texVertices)
      position(0)
    }
  }

  private var glProgram: Int = 0
  override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
    GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1f)

//    val vertexShader: Int = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER).also { shader ->
//      GLES30.glShaderSource(shader, Qutil.loadShaderFile(context, R.raw.pic_vs))
//      GLES30.glCompileShader(shader)
//    }
//    val fragmentShader: Int = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER).also { shader ->
//      GLES30.glShaderSource(shader, Qutil.loadShaderFile(context, R.raw.pic_fs))
//      GLES30.glCompileShader(shader)
//    }
//    glProgram = GLES30.glCreateProgram().also {
//      GLES30.glAttachShader(it, vertexShader)
//      GLES30.glAttachShader(it, fragmentShader)
//      GLES30.glLinkProgram(it)
//    }
//    GLES30.glUseProgram(glProgram)
    glProgram = Qutil.initShader(context,R.raw.pic_vs,R.raw.pic_fs)
    textureId = loadTexture()
  }

  private var textureId = 0

  override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
    GLES30.glViewport(0, 0, width, height)
    Log.i(TAG, "[screen size] width: $width, height: $height")
    val ratio: Float = width.toFloat() / height.toFloat()

    // in the onDrawFrame() method
//    Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1f, 1f, 50f, 100f)
    Matrix.orthoM(projMatrix, 0, -0.5f, 0.5f, -0.5f, 0.5f, 50f, 100f)
    Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 90f, 0f, 0f, 0f, 0f, 1f, 0f)
//      Matrix.setRotateM(modelMatrix, 0, 20f, 0f, 0f, 0f)
//    Matrix.translateM(modelMatrix, 0, 0.5f, 0f, 0f)
//      Matrix.rotateM(modelMatrix, 0, 90f, 0f, 0f, 1f)


  }


  override fun onDrawFrame(gl: GL10) {
    GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

    // 顶点坐标数据
    val a_Position = GLES30.glGetAttribLocation(glProgram, "a_Position")
    GLES30.glEnableVertexAttribArray(a_Position);
    GLES30.glVertexAttribPointer(
      a_Position,
      3,
      GLES30.GL_FLOAT,
      false,
      Float.SIZE_BYTES * 3,
      vertexBuffer
    )

    // 纹理数据
    val a_TextureCoord = GLES30.glGetAttribLocation(glProgram, "a_TextureCoord")
    GLES30.glEnableVertexAttribArray(a_TextureCoord)
    GLES30.glVertexAttribPointer(
      a_TextureCoord,
      2,
      GLES30.GL_FLOAT,
      false,
      Float.SIZE_BYTES * 2,
      texBuffer
    )
    // 激活纹理
    GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)

    // 矩阵
    Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, viewMatrix, 0)
    Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, modelMatrix, 0)
    val u_MvpMatrix = GLES30.glGetUniformLocation(glProgram, "u_MvpMatrix")
    GLES30.glUniformMatrix4fv(u_MvpMatrix, 1, false, mvpMatrix, 0)


    GLES30.glDrawElements(
      GLES30.GL_TRIANGLES,
      vertexIndex.size,
      GLES30.GL_UNSIGNED_INT,
      vertexIndexBuffer
    )

    //禁止顶点数组的句柄
    GLES30.glDisableVertexAttribArray(a_Position);
    GLES30.glDisableVertexAttribArray(a_TextureCoord);
  }

  private val TAG = "glgl"

  private fun loadTexture(): Int {
    val textureIds = IntArray(1)
    GLES30.glGenTextures(1, textureIds, 0)
    if (textureIds[0] == 0) {
      Log.e(TAG, "create texture object failed!")
      return 0
    }

    val options = BitmapFactory.Options().apply {
      inScaled = false
    }
    val bitmap =
      BitmapFactory.decodeResource(context.resources, R.drawable.test_texture, options)
        ?: run {
          GLES30.glDeleteTextures(1, textureIds, 0)
          Log.e(TAG, "加载bitmap错误")
          return 0
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

    // 释放Bitmap
    bitmap.recycle()
    // 解绑
    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)
    return textureIds[0]
  }

}
