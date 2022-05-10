package com.vegeta.glstudy

import android.content.Context
import android.content.res.Resources
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.opengl.GLES30.*
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.core.content.getSystemService
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

open class SphereSurfaceView : GLSurfaceView {


  private val screenWidth = Resources.getSystem().displayMetrics.widthPixels
  private val screenHeight = Resources.getSystem().displayMetrics.widthPixels

  private val TOUCH_SCALE_FACTOR_X = 60f / screenWidth
  private val TOUCH_SCALE_FACTOR_Y = TOUCH_SCALE_FACTOR_X * screenWidth / screenHeight
  private var lastMoveX = 0f
  private var lastMoveY = 0f

  constructor(context: Context?) : super(context)
  constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)


  override fun onTouchEvent(event: MotionEvent): Boolean {
    val x = event.x
    val y = event.y
    when (event.action) {
      MotionEvent.ACTION_MOVE -> {
        val dx = x - lastMoveX
        val dy = y - lastMoveY
        val angleX = dx * TOUCH_SCALE_FACTOR_X
        val angleY = dy * TOUCH_SCALE_FACTOR_Y
        Log.i(TAG, "angleX: $angleX, angleY: $angleY")
        val rotateM = FloatArray(16).apply { Matrix.setIdentityM(this, 0) }
        Matrix.rotateM(rotateM, 0, -angleX, 0f, 1f, 0f)
        Matrix.rotateM(rotateM, 0, -angleY, 1f, 0f, 0f)
        Matrix.multiplyMM(viewMatrix, 0, rotateM, 0, viewMatrix, 0)
        requestRender()
        lastMoveX = x
        lastMoveY = y
      }
      MotionEvent.ACTION_DOWN -> {
        lastMoveX = x
        lastMoveY = y
      }
      MotionEvent.ACTION_UP -> {
        lastMoveX = x
        lastMoveY = y
      }
    }

    return true
  }

  override fun onResume() {
    super.onResume()
    registerSensor()
  }

  override fun onPause() {
    super.onPause()
    unregisterSensor()
  }

  //<editor-fold desc="传感器">
  private val sensorManager by lazy {
    context?.getSystemService<SensorManager>()
  }
  private val sensorListener = object : SensorEventListener {
    private val NS2S = 1.0f / 1000000000.0f
    private var timestamp = 0L
    private val deltaRotationVector = FloatArray(4)
    override fun onSensorChanged(event: SensorEvent) {
      // This time step's delta rotation to be multiplied by the current rotation
      // after computing it from the gyro sample data.
//      if (timestamp != 0L) {
      val dT = (event.timestamp - timestamp) * NS2S
      timestamp = event.timestamp

      var axisX = event.values[0] // x轴 角速度
      var axisY = event.values[1] // y轴 角速度
      var axisZ = event.values[2] // z轴 角速度
//        Log.v(TAG, "角速度: ($axisX, $axisY, $axisZ)")

      // Calculate the angular speed of the sample
      // 向量的模
      val omegaMagnitude = sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ)
//        Log.v(TAG, "  向量模: $omegaMagnitude")

      // Normalize the rotation vector if it's big enough to get the axis
      // 向量归一化
      if (omegaMagnitude > 0.01) {
        axisX /= omegaMagnitude
        axisY /= omegaMagnitude
        axisZ /= omegaMagnitude
      } else {
        return
      }
      Log.v(TAG, "    向量归一化: ($axisX, $axisY, $axisZ)")

      // Integrate around this axis with the angular speed by the time step
      // in order to get a delta rotation from this sample over the time step
      // We will convert this axis-angle representation of the delta rotation
      // into a quaternion before turning it into the rotation matrix.
      val thetaOverTwo = omegaMagnitude * dT / 2.0f
      val sinThetaOverTwo = sin(thetaOverTwo)
      val cosThetaOverTwo = cos(thetaOverTwo)
//        Log.d(TAG, "θ: $thetaOverTwo")
//        Log.d(TAG, "sin θ: $sinThetaOverTwo")
//        Log.d(TAG, "cos θ: $cosThetaOverTwo")
      deltaRotationVector[0] = sinThetaOverTwo * axisX
      deltaRotationVector[1] = sinThetaOverTwo * axisY
      deltaRotationVector[2] = sinThetaOverTwo * axisZ
      deltaRotationVector[3] = cosThetaOverTwo
//        Log.i(
//          TAG,
//          "vec4: (${deltaRotationVector[0]},${deltaRotationVector[1]},${deltaRotationVector[2]},${deltaRotationVector[3]})"
//        )
//      }
      val deltaRotationMatrix = FloatArray(16)
      for (i in deltaRotationVector.indices) {
        deltaRotationVector[i] = deltaRotationVector[i] * 0.85f
      }
      SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector)
//      Log.w(
//        TAG,
//        "matrix:\n\t${deltaRotationMatrix[0]}, \t${deltaRotationMatrix[4]}, \t${deltaRotationMatrix[8]}, \t${deltaRotationMatrix[12]},\n" +
//            "\t${deltaRotationMatrix[1]}, \t${deltaRotationMatrix[5]}, \t${deltaRotationMatrix[9]}, \t${deltaRotationMatrix[13]},\n" +
//            "\t${deltaRotationMatrix[2]}, \t${deltaRotationMatrix[6]}, \t${deltaRotationMatrix[10]}, \t${deltaRotationMatrix[14]},\n" +
//            "\t${deltaRotationMatrix[3]}, \t${deltaRotationMatrix[7]}, \t${deltaRotationMatrix[11]}, \t${deltaRotationMatrix[15]},"
//      )
      // User code should concatenate the delta rotation we computed with the current
      // rotation in order to get the updated rotation.
      // rotationCurrent = rotationCurrent * deltaRotationMatrix;
      Matrix.multiplyMM(viewMatrix, 0, deltaRotationMatrix, 0, viewMatrix, 0)
      logMatrix(viewMatrix, "viewMatrix")
      requestRender()

//      val f4 = {f:Float-> String.format("% .4f",f)}
//      val info = viewMatrix.map { f4(it) }
//      Log.w(
//        TAG,
//        "matrix:\n\t${info[0]}, \t${info[4]}, \t${info[8]}, \t${info[12]},\n" +
//            "\t${info[1]}, \t${info[5]}, \t${info[9]}, \t${info[13]},\n" +
//            "\t${info[2]}, \t${info[6]}, \t${info[10]}, \t${info[14]},\n" +
//            "\t${info[3]}, \t${info[7]}, \t${info[11]}, \t${info[15]},"
//      )
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
  }

  private fun registerSensor() {
    sensorManager?.registerListener(
      sensorListener,
      sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
      SensorManager.SENSOR_DELAY_NORMAL
    )
  }

  private fun unregisterSensor() {
    sensorManager?.unregisterListener(sensorListener)
  }
  //</editor-fold>

}

val viewMatrix = FloatArray(16).apply { Matrix.setIdentityM(this, 0) }

private fun logMatrix(m: FloatArray, prefix: String = "matrix") {
  val f4 = { f: Float -> String.format("% .4f", f) }
  val info = m.map { f4(it) }
  Log.w(
    TAG,
    "$prefix:\n\t${info[0]}, \t${info[4]}, \t${info[8]}, \t${info[12]},\n" +
        "\t${info[1]}, \t${info[5]}, \t${info[9]}, \t${info[13]},\n" +
        "\t${info[2]}, \t${info[6]}, \t${info[10]}, \t${info[14]},\n" +
        "\t${info[3]}, \t${info[7]}, \t${info[11]}, \t${info[15]},"
  )
}

class SphereRenderer(val context: Context, val path: String) : GLSurfaceView.Renderer {
  private var mProgramHandle = 0
  private var vPositionLoc = 0
  private var texCoordLoc = 0
  private var mvpMatrixLoc = 0
  private var textureLoc = 0
  override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
    glClearColor(0F, 0F, 0F, 1F)
    glEnable(GL_DEPTH_TEST)

    mProgramHandle = Qutil.initShader(context, R.raw.sphere_vs, R.raw.sphere_fs)
    generateSphere(4F, 128, 256)
    //获取vPosition索引
    vPositionLoc = glGetAttribLocation(mProgramHandle, "a_Position")
    texCoordLoc = glGetAttribLocation(mProgramHandle, "a_TexCoord")
    mvpMatrixLoc = glGetUniformLocation(mProgramHandle, "mvpMatrix")
    textureLoc = glGetUniformLocation(mProgramHandle, "u_Texture")

    val a = Qutil.loadTexture(context, path, R.drawable.senery)
    textureId = a[0]
  }


  private val mMvpMatrix = FloatArray(16).apply { Matrix.setIdentityM(this, 0) }
  private val projectionMatrix = FloatArray(16).apply { Matrix.setIdentityM(this, 0) }
  private val modelMatrix = FloatArray(16).apply { Matrix.setIdentityM(this, 0) }

  override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
    glViewport(0, 0, width, height)
    val ratio = width.toFloat() / height

//    observeOut(ratio)
    observeIn(ratio)
  }


  private var textureId = 0


  private lateinit var vertexBuffer: FloatBuffer
  private lateinit var texBuffer: FloatBuffer
  private lateinit var mIndicesBuffer: ShortBuffer
  private var indicesNum = 0

  override fun onDrawFrame(gl: GL10?) {
    glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

    glUseProgram(mProgramHandle)
    //设置顶点数据
    vertexBuffer.position(0)
    glEnableVertexAttribArray(vPositionLoc)
    glVertexAttribPointer(vPositionLoc, 3, GL_FLOAT, false, 0, vertexBuffer)

    //设置纹理顶点数据
    texBuffer.position(0)
    glEnableVertexAttribArray(texCoordLoc)
    glVertexAttribPointer(texCoordLoc, 2, GL_FLOAT, false, 0, texBuffer)

    //设置纹理
    glActiveTexture(GL_TEXTURE0)
    glBindTexture(GL_TEXTURE_2D, textureId)
    glUniform1i(textureLoc, 0)

    updateMvpMatrix()
    glUniformMatrix4fv(mvpMatrixLoc, 1, false, mMvpMatrix, 0)


    glDrawElements(
      GL_TRIANGLES,
      indicesNum,
      GL_UNSIGNED_SHORT,
      mIndicesBuffer
    )
  }


  fun generateSphere(radius: Float, rings: Int, sectors: Int) {
    val PI = Math.PI.toFloat()
    val PI_2 = (Math.PI / 2).toFloat()

    val R = 1f / rings.toFloat()
    val S = 1f / sectors.toFloat()
    var r: Short
    var s: Short
    var x: Float
    var y: Float
    var z: Float

    val numPoint = (rings + 1) * (sectors + 1)
    val vertexs = FloatArray(numPoint * 3)//3D坐标
    val texcoords = FloatArray(numPoint * 2)//2D纹理
    val indices = ShortArray(numPoint * 6)

    var t = 0
    var v = 0
    r = 0
    while (r < rings + 1) {
      s = 0
      while (s < sectors + 1) {
        x = cos((2f * PI * s.toFloat() * S)) * sin((PI * r.toFloat() * R))
        y = -sin((-PI_2 + PI * r.toFloat() * R))
        z = sin((2f * PI * s.toFloat() * S)) * sin((PI * r.toFloat() * R))

        texcoords[t++] = s * S
        texcoords[t++] = r * R

        vertexs[v++] = x * radius
        vertexs[v++] = y * radius
        vertexs[v++] = z * radius
        s++
      }
      r++
    }

    var counter = 0
    val sectorsPlusOne = sectors + 1
    r = 0
    while (r < rings) {
      s = 0
      while (s < sectors) {
        indices[counter++] = (r * sectorsPlusOne + s).toShort()       //(a)
        indices[counter++] = ((r + 1) * sectorsPlusOne + s).toShort()    //(b)
        indices[counter++] = (r * sectorsPlusOne + (s + 1)).toShort()  // (c)
        indices[counter++] = (r * sectorsPlusOne + (s + 1)).toShort()  // (c)
        indices[counter++] = ((r + 1) * sectorsPlusOne + s).toShort()    //(b)
        indices[counter++] = ((r + 1) * sectorsPlusOne + (s + 1)).toShort()  // (d)
        s++
      }
      r++
    }

    vertexBuffer = Qutil.array2Buffer(vertexs)
    texBuffer = Qutil.array2Buffer(texcoords)
    mIndicesBuffer = Qutil.array2Buffer(indices)
    indicesNum = indices.size
  }

  /**
   * 从球体外部观察
   */
//  private fun observeOut(ratio: Float) {
//    Matrix.setLookAtM(
//      viewMatrix, 0,
//      0F, 15F, 25f,
//      0F, 0F, 0F,
//      0F, 1F, 0F
//    )
//    Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 5f, 100f)
//  }

  /**
   * 在球体内部观察
   */
  private fun observeIn(ratio: Float) {
    Matrix.setLookAtM(
      viewMatrix, 0,
      0F, 0F, 0f,
      4F, 0F, 0F,
      0F, 1F, 0F
    )
    logMatrix(viewMatrix, "init viewMatrix")
//    Matrix.perspectiveM(projectionMatrix, 0, 80f, ratio, 1f, 10f)
    Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 0.8f, 10f)

  }

  var currentRotateDegree = 0F
  fun updateMvpMatrix() {
//    Matrix.setIdentityM(modelMatrix, 0)
//    Matrix.rotateM(modelMatrix, 0, currentRotateDegree++, 0F, 1F, 0F)
//    Log.d(TAG, "updateMvpMatrix: [currentRotateDegree: $currentRotateDegree]")
    val mTempMvMatrix = FloatArray(16)
    Matrix.setIdentityM(mTempMvMatrix, 0)
    Matrix.multiplyMM(mTempMvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
    Matrix.multiplyMM(mMvpMatrix, 0, projectionMatrix, 0, mTempMvMatrix, 0)
  }


}

//private val sensorListener = object : SensorEventListener {
//  private val NS2S = 1.0f / 1000000000.0f;
//  private var timestamp = 0L;
//  private val deltaRotationVector = FloatArray(4)
//  override fun onSensorChanged(event: SensorEvent) {
//    // This time step's delta rotation to be multiplied by the current rotation
//    // after computing it from the gyro sample data.
//    if (timestamp != 0L) {
//      val dT = (event.timestamp - timestamp) * NS2S
//      // Axis of the rotation sample, not normalized yet.
//      var axisX = event.values[0]
//      var axisY = event.values[1]
//      var axisZ = event.values[2]
//
//      // Calculate the angular speed of the sample
//      val omegaMagnitude = sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);
//
//      // Normalize the rotation vector if it's big enough to get the axis
//      if (omegaMagnitude > 0.00001) {
//        axisX /= omegaMagnitude
//        axisY /= omegaMagnitude
//        axisZ /= omegaMagnitude
//      }
//
//      // Integrate around this axis with the angular speed by the time step
//      // in order to get a delta rotation from this sample over the time step
//      // We will convert this axis-angle representation of the delta rotation
//      // into a quaternion before turning it into the rotation matrix.
//      val thetaOverTwo = omegaMagnitude * dT / 2.0f
//      val sinThetaOverTwo = sin(thetaOverTwo)
//      val cosThetaOverTwo = cos(thetaOverTwo)
//      deltaRotationVector[0] = sinThetaOverTwo * axisX
//      deltaRotationVector[1] = sinThetaOverTwo * axisY
//      deltaRotationVector[2] = sinThetaOverTwo * axisZ
//      deltaRotationVector[3] = cosThetaOverTwo
//    }
//    timestamp = event.timestamp
//    val deltaRotationMatrix = FloatArray(16)
//    SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector)
//    // User code should concatenate the delta rotation we computed with the current
//    // rotation in order to get the updated rotation.
//    // rotationCurrent = rotationCurrent * deltaRotationMatrix;
////        Matrix.multiplyMM(modelMatrix, 0, modelMatrix, 0, deltaRotationMatrix, 0)
//  }
//
//
//  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
//  }
//}

