package com.vegeta.glstudy

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class TexRenderer(val context: Context) : GLSurfaceView.Renderer {
//  private lateinit var triangle: Triangle

  val mvpMatrix = FloatArray(16)
  val projMatrix = FloatArray(16)
  val viewMatrix = FloatArray(16)
  val modelMatrix = FloatArray(16).apply { Matrix.setIdentityM(this, 0) }

  private val vertexShaderCode = """
#version 300 es
layout (location = 0) in vec4 a_Position;
layout (location = 1) in vec2 a_TextureCoord;
uniform mat4 u_MvpMatrix;
//输出纹理坐标(s,t)
out vec2 vTexCoord;
void main() {
    gl_Position  = u_MvpMatrix*a_Position;
    gl_PointSize = 10.0;
    vTexCoord = a_TextureCoord;
}
  """


  private val fragmentShaderCode = """
#version 300 es
precision mediump float;
uniform sampler2D uTextureUnit;
//接收刚才顶点着色器传入的纹理坐标(s,t)
in vec2 vTexCoord;
out vec4 vFragColor;
void main() {
    vFragColor = texture(uTextureUnit,vTexCoord);
}
  """


  /**
   * 顶点坐标
   */
  var POSITION_VERTICES = floatArrayOf(
    0f, 0f, 0f,     //顶点坐标V0
    1f, 1f, 0f,     //顶点坐标V1
    -1f, 1f, 0f,    //顶点坐标V2
    -1f, -1f, 0f,   //顶点坐标V3
    1f, -1f, 0f     //顶点坐标V4
  )
  val COORDS_PER_VERTEX = 3
  private val vertexCount: Int = POSITION_VERTICES.size / COORDS_PER_VERTEX

  private var vertexBuffer: FloatBuffer =
    ByteBuffer.allocateDirect(POSITION_VERTICES.size * Float.SIZE_BYTES).run {
      order(ByteOrder.nativeOrder())
      asFloatBuffer().apply {
        put(POSITION_VERTICES)
        position(0)
      }
    }

  /**
   * 顶点索引
   */
  var POSITION_VERTEX_INDEX = intArrayOf(
    0, 1, 2,  //V0,V1,V2 三个顶点组成一个三角形
    0, 2, 3,  //V0,V2,V3 三个顶点组成一个三角形
    0, 3, 4,  //V0,V3,V4 三个顶点组成一个三角形
    0, 4, 1   //V0,V4,V1 三个顶点组成一个三角形
  )

  private val vertexIndexBuffer =
    ByteBuffer.allocateDirect(POSITION_VERTEX_INDEX.size * Int.SIZE_BYTES).run {
      order(ByteOrder.nativeOrder())
      asIntBuffer().apply {
        put(POSITION_VERTEX_INDEX)
        position(0)
      }
    }

  /**
   * 纹理坐标
   */
  var TEX_VERTICES = floatArrayOf(
    0.5f, 0.5f, //纹理坐标V0
    1f, 0f,     //纹理坐标V1
    0f, 0f,     //纹理坐标V2
    0f, 1.0f,   //纹理坐标V3
    1f, 1.0f    //纹理坐标V4
  )

  private var texBuffer = ByteBuffer.allocateDirect(TEX_VERTICES.size * Float.SIZE_BYTES).run {
    order(ByteOrder.nativeOrder())
    asFloatBuffer().apply {
      put(TEX_VERTICES)
      position(0)
    }
  }

  private var glProgram: Int = 0
  override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
    GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1f)

    val vertexShader: Int = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER).also { shader ->
//      GLES30.glShaderSource(shader, loadShaderFile(context, R.raw.pap_vs))
      GLES30.glShaderSource(shader, vertexShaderCode)
      GLES30.glCompileShader(shader)
    }
    val fragmentShader: Int = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER).also { shader ->
//      GLES30.glShaderSource(shader, loadShaderFile(context, R.raw.pap_fs))
      GLES30.glShaderSource(shader, fragmentShaderCode)
      GLES30.glCompileShader(shader)
    }
    glProgram = GLES30.glCreateProgram().also {
      GLES30.glAttachShader(it, vertexShader)
      GLES30.glAttachShader(it, fragmentShader)
      GLES30.glLinkProgram(it)
    }
    GLES30.glUseProgram(glProgram)

    textureId = loadTexture()
  }

  private var textureId = 0

  override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
    GLES30.glViewport(0, 0, width, height)
    val ratio: Float = width.toFloat() / height.toFloat()

    // in the onDrawFrame() method
    Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1f, 1f, 50f, 100f)
    Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 60f, 0f, 0f, 0f, 0f, 1f, 0f)
//      Matrix.setRotateM(modelMatrix, 0, 20f, 0f, 0f, 0f)
//      Matrix.translateM(modelMatrix, 0, 0.5f, 0f, 0f)
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


    //绘制三个点
    //GLES30.glDrawArrays(GLES30.GL_POINTS, 0, POSITION_COMPONENT_COUNT);

    //绘制三条线
    //    GLES30.glLineWidth(3f);//设置线宽
    //    GLES30.glDrawArrays(GLES30.GL_LINE_LOOP, 0, 3);

    //绘制三角形
//      GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount);
    GLES30.glDrawElements(
      GLES30.GL_TRIANGLES,
      POSITION_VERTEX_INDEX.size,
      GLES30.GL_INT,
      vertexIndexBuffer
    )

    //禁止顶点数组的句柄
    GLES30.glDisableVertexAttribArray(a_Position);
    GLES30.glDisableVertexAttribArray(a_TextureCoord);
  }

  private val TAG = "glgl"

  private fun loadShaderFile(context: Context, resId: Int): String {
    val sb = StringBuilder()

    val inputStream = context.resources.openRawResource(resId)
    val inputStreamReader = InputStreamReader(inputStream)
    val bufferedReader = BufferedReader(inputStreamReader)
    var textLine: String?

    do {
      textLine = bufferedReader.readLine()
      sb.append("$textLine\n")
    } while (textLine != null)

    return sb.toString()
  }

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
          Log.e(TAG,"加载bitmap错误")
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
