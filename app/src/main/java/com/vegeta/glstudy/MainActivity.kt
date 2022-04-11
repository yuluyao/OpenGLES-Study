package com.vegeta.glstudy

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.min

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    requestWindowFeature(Window.FEATURE_NO_TITLE)
//    window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)

    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)


//    setContentView(R.layout.activity_main)


    val renderer = GLRenderer()
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


