package com.vegeta.glstudy

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import com.google.android.material.shape.TriangleEdgeTreatment
import kotlin.math.min

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    requestWindowFeature(Window.FEATURE_NO_TITLE)
//    window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)

    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)


//    setContentView(R.layout.activity_main)


    val renderer = TriangleRenderer(this)
    val glSurfaceView = GLSurfaceView(this).apply {
      setEGLContextClientVersion(3)
      setRenderer(renderer)

    }
    setContentView(glSurfaceView)

    val sensorManager = getSystemService<SensorManager>()
    val listener = object : SensorEventListener {
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
            renderer.modelMatrix,
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
/*
    sensorManager?.registerListener(
      listener,
      sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
      SensorManager.SENSOR_DELAY_UI
    )
*/

  }


}
