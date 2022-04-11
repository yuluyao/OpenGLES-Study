package com.vegeta.glstudy

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class TriangleRenderer(val context: Context) : GLSurfaceView.Renderer {
  private val TAG="glgl"

  val mvpMatrix = FloatArray(16)
  val projMatrix = FloatArray(16)
  val viewMatrix = FloatArray(16)
  val modelMatrix = FloatArray(16).apply { Matrix.setIdentityM(this, 0) }

  private val vertexShaderCode = """
      attribute vec4 a_Position;
      attribute vec4 a_Color;
      varying vec4 v_Color;
      uniform mat4 u_MvpMatrix;
      void main() {
        gl_Position = u_MvpMatrix * a_Position;
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
      GLES30.glShaderSource(shader, Qutil.loadShaderFile(context,R.raw.triangle_vs))
      GLES30.glCompileShader(shader)
    }
    val fragmentShader: Int = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER).also { shader ->
      GLES30.glShaderSource(shader, Qutil.loadShaderFile(context,R.raw.triangle_fs))
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
    Matrix.frustumM(projMatrix, 0, -ratio, ratio, -1f, 1f, 50f, 100f)
    Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 60f, 0f, 0f, 0f, 0f, 1f, 0f)
//      Matrix.setRotateM(modelMatrix, 0, 20f, 0f, 0f, 0f)
//      Matrix.translateM(modelMatrix, 0, 0.5f, 0f, 0f)
//      Matrix.rotateM(modelMatrix, 0, 90f, 0f, 0f, 1f)


  }

  var triangleCoords = floatArrayOf(     // in counterclockwise order:
    0.0f, 0.622008459f, 0.0f,      // top
    -0.5f, -0.311004243f, 0.0f,    // bottom left
    0.5f, -0.311004243f, 0.0f      // bottom right
  )
  val COORDS_PER_VERTEX = 3
  private val vertexCount: Int = triangleCoords.size / COORDS_PER_VERTEX

  private var vertexBuffer: FloatBuffer =
    ByteBuffer.allocateDirect(triangleCoords.size * Float.SIZE_BYTES).run {
      order(ByteOrder.nativeOrder())
      asFloatBuffer().apply {
        put(triangleCoords)
        position(0)
      }
    }

  //三个顶点的颜色参数
  private val colors = floatArrayOf(
    1.0f, 0.0f, 0.0f, 1.0f,// top, red
    0.0f, 1.0f, 0.0f, 1.0f,// bottom left, green
    0.0f, 0.0f, 1.0f, 1.0f// bottom right, blue
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
    GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vertexCount);

    //禁止顶点数组的句柄
    GLES30.glDisableVertexAttribArray(a_Position);
    GLES30.glDisableVertexAttribArray(a_Color);

//    Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
//    Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, viewMatrix, 0)

  }


}
