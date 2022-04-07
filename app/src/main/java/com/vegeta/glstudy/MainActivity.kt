package com.vegeta.glstudy

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val glSurfaceView = GLSurfaceView(this).apply {
      setEGLContextClientVersion(2)
      setRenderer(GLRenderer())
    }
    setContentView(glSurfaceView)

  }
}

class GLRenderer : GLSurfaceView.Renderer {
  private lateinit var triangle: Triangle

  private val mvpMatrix = FloatArray(16)
  private val projMatrix = FloatArray(16)
  private val viewMatrix = FloatArray(16)

  override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
    GLES20.glClearColor(0f, 0f, 0f, 1f)
    triangle = Triangle()
  }

  override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
    GLES20.glViewport(0, 0, width, height)
    val ratio: Float = width.toFloat() / height.toFloat()

    // this projection matrix is applied to object coordinates
    // in the onDrawFrame() method
    Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
  }

  override fun onDrawFrame(gl: GL10) {
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

    Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
    Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, viewMatrix, 0)

    triangle.draw(mvpMatrix)
  }

}


class Triangle {

  private val vertexShaderCode = """
      attribute vec4 a_Position;
      uniform mat4 u_MVPMatrix;
      void main() {
        gl_Position = u_MVPMatrix * a_Position;
      }
  """


  private val fragmentShaderCode = """
      precision mediump float;
      uniform vec4 u_Color;
      void main() {
        gl_FragColor = u_Color;
      }
  """

  val color = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)


  val COORDS_PER_VERTEX = 3
  var triangleCoords = floatArrayOf(     // in counterclockwise order:
    0.0f, 0.622008459f, 0.0f,      // top
    -0.5f, -0.311004243f, 0.0f,    // bottom left
    0.5f, -0.311004243f, 0.0f      // bottom right
  )

  // (number of coordinate values * 4 bytes per float)
  private var vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(triangleCoords.size * 4).run {
    // use the device hardware's native byte order
    order(ByteOrder.nativeOrder())

    // create a floating point buffer from the ByteBuffer
    asFloatBuffer().apply {
      // add the coordinates to the FloatBuffer
      put(triangleCoords)
      // set the buffer to read the first coordinate
      position(0)
    }
  }

  private var glProgram: Int

  init {
    val vertexShader: Int = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER).also { shader ->
      GLES20.glShaderSource(shader, vertexShaderCode)
      GLES20.glCompileShader(shader)
    }
    val fragmentShader: Int = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER).also { shader ->
      GLES20.glShaderSource(shader, fragmentShaderCode)
      GLES20.glCompileShader(shader)
    }
    glProgram = GLES20.glCreateProgram().also {
      GLES20.glAttachShader(it, vertexShader)
      GLES20.glAttachShader(it, fragmentShader)
      GLES20.glLinkProgram(it)
    }
  }

  private var positionHandle: Int = 0
  private var mColorHandle: Int = 0
  private var vPMatrixHandle: Int = 0

  private val vertexCount: Int = triangleCoords.size / COORDS_PER_VERTEX
  private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex

  fun draw(mvpMatrix: FloatArray) {
    // Add program to OpenGL ES environment
    GLES20.glUseProgram(glProgram)

    // get handle to vertex shader's vPosition member
    positionHandle = GLES20.glGetAttribLocation(glProgram, "a_Position").also {

      // Enable a handle to the triangle vertices
      GLES20.glEnableVertexAttribArray(it)

      // Prepare the triangle coordinate data
      GLES20.glVertexAttribPointer(
        it,
        COORDS_PER_VERTEX,
        GLES20.GL_FLOAT,
        false,
        vertexStride,
        vertexBuffer
      )

      // get handle to fragment shader's vColor member
      mColorHandle = GLES20.glGetUniformLocation(glProgram, "u_Color").also { colorHandle ->

        // Set color for drawing the triangle
        GLES20.glUniform4fv(colorHandle, 1, color, 0)
      }

      vPMatrixHandle = GLES20.glGetUniformLocation(glProgram, "u_MVPMatrix")
      GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0)

      // Draw the triangle
      GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)

      // Disable vertex array
      GLES20.glDisableVertexAttribArray(it)
    }
  }
}

