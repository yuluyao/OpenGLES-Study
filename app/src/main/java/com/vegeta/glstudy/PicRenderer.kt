package com.vegeta.glstudy

import android.content.Context
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.opengl.GLES30.*
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import android.util.Log
import androidx.core.content.getSystemService
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.min

class PicGLSurfaceView(context: Context) : GLSurfaceView(context) {

/*
  private val sensorManager by lazy { context.getSystemService<SensorManager>() }
  private val sensorListener = object : SensorEventListener {
    private val NS2S = 1.0f / 1000000000.0f;
    private var timestamp = 0L;
    private val deltaRotationVector = FloatArray(4)
    override fun onSensorChanged(event: SensorEvent) {
      // This time step's delta rotation to be multiplied by the current rotation
      // after computing it from the gyro sample data.
      if (timestamp != 0L) {
        val dT = (event.timestamp - timestamp) * NS2S
        // Axis of the rotation sample, not normalized yet.
        var axisX = event.values[0]
        var axisY = event.values[1]
//          var axisZ = event.values[2]
//          var axisZ = 0f

        // Calculate the angular speed of the sample
//          val omegaMagnitude = sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);

        // Normalize the rotation vector if it's big enough to get the axis
//          if (omegaMagnitude > 0.00001) {
//            axisX /= omegaMagnitude
//            axisY /= omegaMagnitude
//            axisZ /= omegaMagnitude
//          }

        // Integrate around this axis with the angular speed by the time step
        // in order to get a delta rotation from this sample over the time step
        // We will convert this axis-angle representation of the delta rotation
        // into a quaternion before turning it into the rotation matrix.
//          val thetaOverTwo = omegaMagnitude * dT / 2.0f
//          val sinThetaOverTwo = sin(thetaOverTwo)
//          val cosThetaOverTwo = cos(thetaOverTwo)
//          deltaRotationVector[0] = sinThetaOverTwo * axisX
//          deltaRotationVector[1] = sinThetaOverTwo * axisY
//          deltaRotationVector[2] = sinThetaOverTwo * axisZ
//          deltaRotationVector[3] = cosThetaOverTwo


        val maxAngle = (Math.PI / 3).toFloat()
        val scaleX = min(maxAngle, axisX * dT) / maxAngle
        val scaleY = min(maxAngle, axisY * dT) / maxAngle
//          val modelMatrix = FloatArray(16).apply { Matrix.setIdentityM(this, 0) }
        Matrix.translateM(
          modelMatrix,
          0,
          scaleY * 0.5f,
          -scaleX * 0.5f,
          0f
        )
//          Matrix.multiplyMM(renderer.mvpMatrix, 0, renderer.mvpMatrix, 0, modelMatrix, 0)

      }
      timestamp = event.timestamp
//        val deltaRotationMatrix = FloatArray(16)
//        SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector)
      // User code should concatenate the delta rotation we computed with the current
      // rotation in order to get the updated rotation.
      // rotationCurrent = rotationCurrent * deltaRotationMatrix;
//        Matrix.multiplyMM(renderer.mvpMatrix, 0, renderer.mvpMatrix, 0, deltaRotationMatrix, 0)
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
  }

  private fun registerSensor() {
    sensorManager?.registerListener(
      sensorListener,
      sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
      SensorManager.SENSOR_DELAY_UI
    )

  }
  private fun unregisterSensor() {
    sensorManager?.unregisterListener(sensorListener)
  }
*/
  override fun onResume() {
    super.onResume()
//    registerSensor()
  }
  override fun onPause() {
    super.onPause()
//    unregisterSensor()
  }

}

class PicRenderer(val context: Context) : GLSurfaceView.Renderer {

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
    glClearColor(0.0f, 0.0f, 0.0f, 1f)
    glProgram = Qutil.initShader(context, R.raw.pic_vs, R.raw.pic_fs)
//    textureId = loadTexture()
    val a = Qutil.loadTexture(context, R.drawable.test_texture)
    textureId = a[0]
    bitmapSize[0] = a[1]
    bitmapSize[1] = a[2]

    registerSensor()

  }


  private var textureId = 0
  private val bitmapSize = IntArray(2)

  override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
    glViewport(0, 0, width, height)
    Log.i(TAG, "[screen size] width: $width, height: $height")
    Log.i(TAG, "[bitmap size] width: ${bitmapSize[0]}, height: ${bitmapSize[1]}")
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
    glClear(GL_COLOR_BUFFER_BIT)

    // 顶点坐标数据
    val a_Position = glGetAttribLocation(glProgram, "a_Position")
    glEnableVertexAttribArray(a_Position);
    glVertexAttribPointer(
      a_Position,
      3,
      GL_FLOAT,
      false,
      Float.SIZE_BYTES * 3,
      vertexBuffer
    )

    // 纹理数据
    val a_TextureCoord = glGetAttribLocation(glProgram, "a_TextureCoord")
    glEnableVertexAttribArray(a_TextureCoord)
    glVertexAttribPointer(
      a_TextureCoord,
      2,
      GL_FLOAT,
      false,
      Float.SIZE_BYTES * 2,
      texBuffer
    )
    // 激活纹理
    glActiveTexture(GL_TEXTURE0)
    glBindTexture(GL_TEXTURE_2D, textureId)

    // 矩阵
    Matrix.multiplyMM(mvpMatrix, 0, projMatrix, 0, viewMatrix, 0)
    Matrix.multiplyMM(mvpMatrix, 0, mvpMatrix, 0, modelMatrix, 0)
    val u_MvpMatrix = glGetUniformLocation(glProgram, "u_MvpMatrix")
    glUniformMatrix4fv(u_MvpMatrix, 1, false, mvpMatrix, 0)


    glDrawElements(
      GL_TRIANGLES,
      vertexIndex.size,
      GL_UNSIGNED_INT,
      vertexIndexBuffer
    )

    //禁止顶点数组的句柄
    glDisableVertexAttribArray(a_Position);
    glDisableVertexAttribArray(a_TextureCoord);
  }

  private val TAG = "glgl"

  private fun loadTexture(): Int {
    val textureIds = IntArray(1)
    glGenTextures(1, textureIds, 0)
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
          glDeleteTextures(1, textureIds, 0)
          Log.e(TAG, "加载bitmap错误")
          return 0
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
    GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0)
    // 生成mipmap
    glGenerateMipmap(GL_TEXTURE_2D)

    // 释放Bitmap
    bitmap.recycle()
    // 解绑
    glBindTexture(GL_TEXTURE_2D, 0)
    return textureIds[0]
  }

  //<editor-fold desc="传感器">
  private val sensorManager by lazy { context.getSystemService<SensorManager>() }
  private val sensorListener = object : SensorEventListener {
    private val NS2S = 1.0f / 1000000000.0f;
    private var timestamp = 0L;
    private val deltaRotationVector = FloatArray(4)
    override fun onSensorChanged(event: SensorEvent) {
      // This time step's delta rotation to be multiplied by the current rotation
      // after computing it from the gyro sample data.
      if (timestamp != 0L) {
        val dT = (event.timestamp - timestamp) * NS2S
        // Axis of the rotation sample, not normalized yet.
        var axisX = event.values[0]
        var axisY = event.values[1]
//          var axisZ = event.values[2]
//          var axisZ = 0f

        // Calculate the angular speed of the sample
//          val omegaMagnitude = sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);

        // Normalize the rotation vector if it's big enough to get the axis
//          if (omegaMagnitude > 0.00001) {
//            axisX /= omegaMagnitude
//            axisY /= omegaMagnitude
//            axisZ /= omegaMagnitude
//          }

        // Integrate around this axis with the angular speed by the time step
        // in order to get a delta rotation from this sample over the time step
        // We will convert this axis-angle representation of the delta rotation
        // into a quaternion before turning it into the rotation matrix.
//          val thetaOverTwo = omegaMagnitude * dT / 2.0f
//          val sinThetaOverTwo = sin(thetaOverTwo)
//          val cosThetaOverTwo = cos(thetaOverTwo)
//          deltaRotationVector[0] = sinThetaOverTwo * axisX
//          deltaRotationVector[1] = sinThetaOverTwo * axisY
//          deltaRotationVector[2] = sinThetaOverTwo * axisZ
//          deltaRotationVector[3] = cosThetaOverTwo


        val maxAngle = (Math.PI / 3).toFloat()
        val scaleX = min(maxAngle, axisX * dT) / maxAngle
        val scaleY = min(maxAngle, axisY * dT) / maxAngle
//          val modelMatrix = FloatArray(16).apply { Matrix.setIdentityM(this, 0) }
        Matrix.translateM(
          modelMatrix,
          0,
          scaleY * 0.5f,
          -scaleX * 0.5f,
          0f
        )
//          Matrix.multiplyMM(renderer.mvpMatrix, 0, renderer.mvpMatrix, 0, modelMatrix, 0)

      }
      timestamp = event.timestamp
//        val deltaRotationMatrix = FloatArray(16)
//        SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector)
      // User code should concatenate the delta rotation we computed with the current
      // rotation in order to get the updated rotation.
      // rotationCurrent = rotationCurrent * deltaRotationMatrix;
//        Matrix.multiplyMM(renderer.mvpMatrix, 0, renderer.mvpMatrix, 0, deltaRotationMatrix, 0)
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
  }

  private fun registerSensor() {
    sensorManager?.registerListener(
      sensorListener,
      sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
      SensorManager.SENSOR_DELAY_UI
    )

  }
  private fun unregisterSensor() {
    sensorManager?.unregisterListener(sensorListener)
  }
  //</editor-fold>


}
