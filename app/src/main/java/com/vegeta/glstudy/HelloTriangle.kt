package com.vegeta.glstudy

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class HelloTriangle(val context: Context) : GLSurfaceView.Renderer {

  private var glProgram: Int = 0
  override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
    GLES30.glClearColor(0.9f, 0.9f, 0.9f, 1f)

    glProgram = Qutil.initShader(context, R.raw.hello_triangle_vs, R.raw.hello_triangle_fs)
    GLES30.glUseProgram(glProgram)
    
    GLES30.glDisableVertexAttribArray(0)
  }

  override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
    GLES30.glViewport(0, 0, width, height)
  }

  private val vertices = floatArrayOf(
    -0.5f, -0.5f, 0.0f,
    0.5f, -0.5f, 0.0f,
    0.0f, 0.5f, 0.0f
  )
  private val vbo = ByteBuffer.allocateDirect(vertices.size * Float.SIZE_BYTES).run {
    order(ByteOrder.nativeOrder())
    asFloatBuffer().apply {
      put(vertices)
      position(0)
    }
  }

  override fun onDrawFrame(gl: GL10) {

//    val buffers = IntArray(1)
//    GLES30.glGenBuffers(1, buffers, 0)
//    GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo)
//    GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER,vertices.size*Float.SIZE_BYTES,vbo,GLES30.GL_STATIC_DRAW)

    GLES30.glVertexAttribPointer(
      0,
      3,
      GLES30.GL_FLOAT,
      false,
      Float.SIZE_BYTES * 3,
      vbo
    )
    GLES30.glEnableVertexAttribArray(0)

    GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3)
    
    GLES30.glDisableVertexAttribArray(0)


  }
}