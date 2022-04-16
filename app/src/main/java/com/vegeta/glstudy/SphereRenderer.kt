package com.vegeta.glstudy

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import androidx.core.content.getSystemService
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class SphereRenderer(val context: Context) : GLSurfaceView.Renderer {
  private var mProgramHandle = 0
  private var vPositionLoc = 0
  private var texCoordLoc = 0
  private var mvpMatrixLoc = 0
  private var textureLoc = 0
  override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
    GLES30.glClearColor(0F, 0F, 0F, 1F)
    GLES30.glEnable(GLES30.GL_DEPTH_TEST)

    mProgramHandle = Qutil.initShader(context, R.raw.sphere_vs, R.raw.sphere_fs)
    generateSphere(2F, 75, 150)
    //获取vPosition索引
    vPositionLoc = GLES30.glGetAttribLocation(mProgramHandle, "a_Position")
    texCoordLoc = GLES30.glGetAttribLocation(mProgramHandle, "a_TexCoord")
    mvpMatrixLoc = GLES30.glGetUniformLocation(mProgramHandle, "mvpMatrix")
    textureLoc = GLES30.glGetUniformLocation(mProgramHandle, "u_Texture")

    val a = Qutil.loadTexture(context, R.drawable.earth)
//    val a = Qutil.loadTexture(context, R.drawable.test_texture)
//    val a = Qutil.loadTexture(context, R.drawable.senery2)
    textureId = a[0]

    registerSensor()
  }


  private val mMvpMatrix = FloatArray(16).apply { Matrix.setIdentityM(this, 0) }
  private val projectionMatrix = FloatArray(16).apply { Matrix.setIdentityM(this, 0) }
  private val viewMatrix = FloatArray(16).apply { Matrix.setIdentityM(this, 0) }
  private val modelMatrix = FloatArray(16).apply { Matrix.setIdentityM(this, 0) }

  override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
    GLES30.glViewport(0, 0, width, height)
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
    GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)

    GLES30.glUseProgram(mProgramHandle)
    //设置顶点数据
    vertexBuffer.position(0)
    GLES30.glEnableVertexAttribArray(vPositionLoc)
    GLES30.glVertexAttribPointer(vPositionLoc, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer)

    //设置纹理顶点数据
    texBuffer.position(0)
    GLES30.glEnableVertexAttribArray(texCoordLoc)
    GLES30.glVertexAttribPointer(texCoordLoc, 2, GLES30.GL_FLOAT, false, 0, texBuffer)

    //设置纹理
    GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
    GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
    GLES30.glUniform1i(textureLoc, 0)

    updateMvpMatrix()
    GLES30.glUniformMatrix4fv(mvpMatrixLoc, 1, false, mMvpMatrix, 0)

    GLES30.glDrawElements(
      GLES30.GL_TRIANGLES,
      indicesNum,
      GLES30.GL_UNSIGNED_SHORT,
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
    val vertexs = FloatArray(numPoint * 3)
    val texcoords = FloatArray(numPoint * 2)
    val indices = ShortArray(numPoint * 6)

    var t = 0
    var v = 0
    r = 0
    while (r < rings + 1) {
      s = 0
      while (s < sectors + 1) {
        x =
          (Math.cos((2f * PI * s.toFloat() * S).toDouble()) * Math.sin((PI * r.toFloat() * R).toDouble())).toFloat()
        y = -Math.sin((-PI_2 + PI * r.toFloat() * R).toDouble()).toFloat()
        z =
          (Math.sin((2f * PI * s.toFloat() * S).toDouble()) * Math.sin((PI * r.toFloat() * R).toDouble())).toFloat()

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
  private fun observeOut(ratio: Float) {
//    Matrix.setLookAtM(
//      viewMatrix, 0,
//      0F, 0F, 75f,
//      0F, 0F, 0F,
//      0F, 1F, 0F
//    )
//    Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 15f, 100f)
  }

  /**
   * 在球体内部观察
   */
  private fun observeIn(ratio: Float) {
//    Matrix.setLookAtM(
//      viewMatrix, 0,
//      0F, 0F, 0f,
////      0F, 0F, -2F,
//      0F, 1F, 0F
//    )
    Matrix.perspectiveM(projectionMatrix, 0, 60f, ratio, 1f, 10f)

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
        var axisX = event.values[0] // x轴 角速度
        var axisY = event.values[1] // y轴 角速度
        var axisZ = event.values[2] // z轴 角速度
        Log.v(TAG, "角速度: ($axisX, $axisY, $axisZ)")

        // Calculate the angular speed of the sample
        // 向量的模
        val omegaMagnitude = sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ)
        Log.v(TAG, "  向量模: $omegaMagnitude")

        // Normalize the rotation vector if it's big enough to get the axis
        // 向量归一化
        if (omegaMagnitude > 0.000001) {
          axisX /= omegaMagnitude
          axisY /= omegaMagnitude
          axisZ /= omegaMagnitude
        } else {
          return
        }
        Log.d(TAG, "    向量归一化: ($axisX, $axisY, $axisZ)")

        // Integrate around this axis with the angular speed by the time step
        // in order to get a delta rotation from this sample over the time step
        // We will convert this axis-angle representation of the delta rotation
        // into a quaternion before turning it into the rotation matrix.
        val thetaOverTwo = omegaMagnitude * dT / 2.0f
        val sinThetaOverTwo = sin(thetaOverTwo)
        val cosThetaOverTwo = cos(thetaOverTwo)
        Log.d(TAG, "θ: $thetaOverTwo")
        Log.d(TAG, "sin θ: $sinThetaOverTwo")
        Log.d(TAG, "cos θ: $cosThetaOverTwo")
        deltaRotationVector[0] = sinThetaOverTwo * axisX
        deltaRotationVector[1] = sinThetaOverTwo * axisY
        deltaRotationVector[2] = sinThetaOverTwo * axisZ
        deltaRotationVector[3] = cosThetaOverTwo
        Log.i(
          TAG,
          "vec4: (${deltaRotationVector[0]},${deltaRotationVector[1]},${deltaRotationVector[2]},${deltaRotationVector[3]})"
        )
      }
      timestamp = event.timestamp
      val deltaRotationMatrix = FloatArray(16)
      SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector)
      Log.w(
        TAG,
        "matrix:\n\t${deltaRotationMatrix[0]}, \t${deltaRotationMatrix[4]}, \t${deltaRotationMatrix[8]}, \t${deltaRotationMatrix[12]},\n" +
            "\t${deltaRotationMatrix[1]}, \t${deltaRotationMatrix[5]}, \t${deltaRotationMatrix[9]}, \t${deltaRotationMatrix[13]},\n" +
            "\t${deltaRotationMatrix[2]}, \t${deltaRotationMatrix[6]}, \t${deltaRotationMatrix[10]}, \t${deltaRotationMatrix[14]},\n" +
            "\t${deltaRotationMatrix[3]}, \t${deltaRotationMatrix[7]}, \t${deltaRotationMatrix[11]}, \t${deltaRotationMatrix[15]},"
      )
      // User code should concatenate the delta rotation we computed with the current
      // rotation in order to get the updated rotation.
      // rotationCurrent = rotationCurrent * deltaRotationMatrix;
      Matrix.multiplyMM(viewMatrix,0,deltaRotationMatrix,0,viewMatrix,0)

      // 直接用旋转矩阵乘向量
//      var vx = viewCenterVec3[0]
//      var vy = viewCenterVec3[1]
//      var vz = viewCenterVec3[2]
//      val vm = sqrt(vx * vx + vy * vy + vz * vz)
//      vx /= vm
//      vy /= vm
//      vz /= vm
//      val vec4 = floatArrayOf(vx, vy, vz, 1f)//原观察点
//      Matrix.multiplyMV(vec4, 0, deltaRotationMatrix, 0, vec4, 0)//对观察点旋转
//      viewCenterVec3[0] = vec4[0] / vec4[3]
//      viewCenterVec3[1] = vec4[1] / vec4[3]
//      viewCenterVec3[2] = vec4[2] / vec4[3]


      // 手动计算，用四元数旋转一个向量，u -> u1
//      var ux = viewCenterVec3[0]
//      var uy = viewCenterVec3[1]
//      var uz = viewCenterVec3[2]
//      val q = deltaRotationVector
//      val qx = q[0]
//      val qy = q[2]
//      val qz = q[2]
//      val w = q[3]
//      val temp1 = floatArrayOf(
//        (2 * w * w - 1) * ux,
//        (2 * w * w - 1) * uy,
//        (2 * w * w - 1) * uz
//      )
//      val temp2 = floatArrayOf(
//        2 * w * (uy * qz - uz * qy),
//        2 * w * (uz * qx - ux * qz),
//        2 * w * (ux * qy - uy * qx)
//      )
//      val temp3 = floatArrayOf(
//        2 * (ux * qx + uy * qy + uz * qz) * qx,
//        2 * (ux * qx + uy * qy + uz * qz) * qy,
//        2 * (ux * qx + uy * qy + uz * qz) * qz
//      )
//      val u1 = floatArrayOf(
//        temp1[0] + temp2[0] + temp3[0],
//        temp1[1] + temp2[1] + temp3[1],
//        temp1[2] + temp2[2] + temp3[2],
//      )
//      viewCenterVec3[0] = u1[0]
//      viewCenterVec3[1] = u1[1]
//      viewCenterVec3[2] = u1[2]

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
