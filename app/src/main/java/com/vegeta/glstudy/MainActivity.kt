package com.vegeta.glstudy

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    requestWindowFeature(Window.FEATURE_NO_TITLE)
//    window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)

    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)


    setContentView(R.layout.activity_main)


    val glSurfaceView = GLSurfaceView(this).apply {
      setEGLContextClientVersion(3)
      setRenderer(GLRenderer())
    }
    setContentView(glSurfaceView)

  }
}

class GLRenderer : GLSurfaceView.Renderer {
//  private lateinit var triangle: Triangle

  private val mvpMatrix = FloatArray(16)
  private val projMatrix = FloatArray(16)
  private val viewMatrix = FloatArray(16)

  private val vertexShaderCode = """
      attribute vec4 a_Position;
      attribute vec4 a_Color;
      varying vec4 v_Color;
      void main() {
        gl_Position = a_Position;
        v_Color = a_Color;
      }
  """


  private val fragmentShaderCode = """
      precision mediump float;
      varying vec4 v_Color;
      void main() {
        gl_FragColor = v_Color;
      }
  """


  private var glProgram: Int = 0
  override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
    GLES30.glClearColor(0.2f, 0.2f, 0.2f, 1f)

    val vertexShader: Int = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER).also { shader ->
      GLES30.glShaderSource(shader, vertexShaderCode)
      GLES30.glCompileShader(shader)
    }
    val fragmentShader: Int = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER).also { shader ->
      GLES30.glShaderSource(shader, fragmentShaderCode)
      GLES30.glCompileShader(shader)
    }
    glProgram = GLES30.glCreateProgram().also {
      GLES30.glAttachShader(it, vertexShader)
      GLES30.glAttachShader(it, fragmentShader)
      GLES30.glLinkProgram(it)
    }

    GLES30.glUseProgram(glProgram)
  }

  override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
    GLES30.glViewport(0, 0, width, height)
    val ratio: Float = width.toFloat() / height.toFloat()

    // in the onDrawFrame() method
//    Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
  }

  var triangleCoords = floatArrayOf(     // in counterclockwise order:
    0.0f, 0.622008459f, 0.0f,      // top
    -0.5f, -0.311004243f, 0.0f,    // bottom left
    0.5f, -0.311004243f, 0.0f      // bottom right
  )
  val COORDS_PER_VERTEX = 3
  private val vertexCount: Int = triangleCoords.size / COORDS_PER_VERTEX

  private var vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(triangleCoords.size * Float.SIZE_BYTES).run {
    order(ByteOrder.nativeOrder())
    asFloatBuffer().apply {
      put(triangleCoords)
      position(0)
    }
  }

  //三个顶点的颜色参数
  private val colors = floatArrayOf(
    1.0f, 0.0f, 0.0f, 1.0f,// top
    0.0f, 1.0f, 0.0f, 1.0f,// bottom left
    0.0f, 0.0f, 1.0f, 1.0f// bottom right
  )
  private var colorBuffer = ByteBuffer.allocateDirect(colors.size * Float.SIZE_BYTES).run {
    order(ByteOrder.nativeOrder())
    asFloatBuffer().apply {
      put(colors)
      position(0)
    }
  }

  override fun onDrawFrame(gl: GL10) {
    GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

    //准备坐标数据
    val a_Position = GLES30.glGetAttribLocation(glProgram, "a_Position")
    GLES30.glVertexAttribPointer(
      a_Position,
      COORDS_PER_VERTEX,
      GLES30.GL_FLOAT,
      false,
      0,
      vertexBuffer
    );
    //启用顶点位置句柄
    GLES30.glEnableVertexAttribArray(a_Position);

    //准备颜色数据
    val a_Color = GLES30.glGetAttribLocation(glProgram, "a_Color")
    GLES30.glVertexAttribPointer(a_Color, 4, GLES30.GL_FLOAT, false, 0, colorBuffer);
    //启用顶点颜色句柄
    GLES30.glEnableVertexAttribArray(a_Color);

    //绘制三个点
    //GLES30.glDrawArrays(GLES30.GL_POINTS, 0, POSITION_COMPONENT_COUNT);

    //绘制三条线
//    GLES30.glLineWidth(3f);//设置线宽
//    GLES30.glDrawArrays(GLES30.GL_LINE_LOOP, 0, 3);

    //绘制三角形
    GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount);

    //禁止顶点数组的句柄
    GLES30.glDisableVertexAttribArray(a_Position);
    GLES30.glDisableVertexAttribArray(a_Color);

//    Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
//    Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, viewMatrix, 0)

//    triangle.draw(mvpMatrix)
  }

}


//class Triangle {
//
//  private val vertexShaderCode = """
//      attribute vec4 a_Position;
//      uniform mat4 u_MVPMatrix;
//      void main() {
//        gl_Position = u_MVPMatrix * a_Position;
//      }
//  """
//
//
//  private val fragmentShaderCode = """
//      precision mediump float;
//      uniform vec4 u_Color;
//      void main() {
//        gl_FragColor = u_Color;
//      }
//  """
//
//  val color = floatArrayOf(0.63671875f, 0.76953125f, 0.22265625f, 1.0f)
//
//
//  val COORDS_PER_VERTEX = 3
//  var triangleCoords = floatArrayOf(     // in counterclockwise order:
//    0.0f, 0.622008459f, 0.0f,      // top
//    -0.5f, -0.311004243f, 0.0f,    // bottom left
//    0.5f, -0.311004243f, 0.0f      // bottom right
//  )
//
//  // (number of coordinate values * 4 bytes per float)
//  private var vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(triangleCoords.size * 4).run {
//    // use the device hardware's native byte order
//    order(ByteOrder.nativeOrder())
//
//    // create a floating point buffer from the ByteBuffer
//    asFloatBuffer().apply {
//      // add the coordinates to the FloatBuffer
//      put(triangleCoords)
//      // set the buffer to read the first coordinate
//      position(0)
//    }
//  }
//
//  private var glProgram: Int
//
//  init {
//    val vertexShader: Int = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER).also { shader ->
//      GLES30.glShaderSource(shader, vertexShaderCode)
//      GLES30.glCompileShader(shader)
//    }
//    val fragmentShader: Int = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER).also { shader ->
//      GLES30.glShaderSource(shader, fragmentShaderCode)
//      GLES30.glCompileShader(shader)
//    }
//    glProgram = GLES30.glCreateProgram().also {
//      GLES30.glAttachShader(it, vertexShader)
//      GLES30.glAttachShader(it, fragmentShader)
//      GLES30.glLinkProgram(it)
//    }
//  }
//
//  private var positionHandle: Int = 0
//  private var mColorHandle: Int = 0
//  private var vPMatrixHandle: Int = 0
//
//  private val vertexCount: Int = triangleCoords.size / COORDS_PER_VERTEX
//  private val vertexStride: Int = COORDS_PER_VERTEX * 4 // 4 bytes per vertex
//
//  fun draw(mvpMatrix: FloatArray) {
//    // Add program to OpenGL ES environment
//    GLES30.glUseProgram(glProgram)
//
//    // get handle to vertex shader's vPosition member
//    positionHandle = GLES30.glGetAttribLocation(glProgram, "a_Position").also {
//
//      // Enable a handle to the triangle vertices
//      GLES30.glEnableVertexAttribArray(it)
//
//      // Prepare the triangle coordinate data
//      GLES30.glVertexAttribPointer(
//        it,
//        COORDS_PER_VERTEX,
//        GLES30.GL_FLOAT,
//        false,
//        vertexStride,
//        vertexBuffer
//      )
//
//      // get handle to fragment shader's vColor member
//      mColorHandle = GLES30.glGetUniformLocation(glProgram, "u_Color").also { colorHandle ->
//
//        // Set color for drawing the triangle
//        GLES30.glUniform4fv(colorHandle, 1, color, 0)
//      }
//
//      vPMatrixHandle = GLES30.glGetUniformLocation(glProgram, "u_MVPMatrix")
//      GLES30.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0)
//
//      // Draw the triangle
//      GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount)
//
//      // Disable vertex array
//      GLES30.glDisableVertexAttribArray(it)
//    }
//  }
//}


