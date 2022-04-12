package com.vegeta.glstudy

import android.content.Context
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SphereRenderer(val context: Context) : GLSurfaceView.Renderer {
  private var mProgramHandle = 0
  private var vPositionLoc = 0
  private var texCoordLoc = 0
  private var mvpMatrixLoc = 0
  private var textureLoc = 0
  override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
    GLES30.glClearColor(0F, 0F, 0F, 1F)
    mProgramHandle = Qutil.initShader(context, R.raw.sphere_vs, R.raw.sphere_fs)
    generateSphere(2F, 75, 150)
    //获取vPosition索引
    vPositionLoc = GLES30.glGetAttribLocation(mProgramHandle, "a_Position")
    texCoordLoc = GLES30.glGetAttribLocation(mProgramHandle, "a_TexCoord")
    mvpMatrixLoc = GLES30.glGetUniformLocation(mProgramHandle, "mvpMatrix")
    textureLoc = GLES30.glGetUniformLocation(mProgramHandle, "u_Texture")

    val a = Qutil.loadTexture(context,R.drawable.earth)
    textureId=a[0]
  }


  private val mMvpMatrix = FloatArray(16).apply { Matrix.setIdentityM(this, 0) }
  private val projectionMatrix = FloatArray(16).apply { Matrix.setIdentityM(this, 0) }
  private val viewMatrix = FloatArray(16).apply { Matrix.setIdentityM(this, 0) }
   val modelMatrix = FloatArray(16).apply { Matrix.setIdentityM(this, 0) }

  override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
    GLES30.glViewport(0, 0, width, height)

//    Matrix.setIdentityM(viewMatrix, 0)
    Matrix.setLookAtM(
      viewMatrix, 0,
      0F, 5F, 10F,
      0F, 0F, 0F,
      0F, 1F, 0F
    )

//    Matrix.setIdentityM(projectionMatrix, 0)
    val ratio = width.toFloat() / height
    //设置透视投影
    Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 20f)


  }

  private var textureId =0


  private lateinit var vertexBuffer: FloatBuffer
  private lateinit var texBuffer: FloatBuffer
  private lateinit var mIndicesBuffer: ShortBuffer
  private var indicesNum =0

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


  var currentRotateDegree = 0F
  fun updateMvpMatrix() {
    Matrix.setIdentityM(modelMatrix, 0)
    Matrix.rotateM(modelMatrix, 0, currentRotateDegree++, 0F, 1F, 0F)
//    Log.d(TAG, "updateMvpMatrix: [currentRotateDegree: $currentRotateDegree]")
    val mTempMvMatrix = FloatArray(16)
    Matrix.setIdentityM(mTempMvMatrix, 0)
    Matrix.multiplyMM(mTempMvMatrix, 0, viewMatrix, 0, modelMatrix, 0)
    Matrix.multiplyMM(mMvpMatrix, 0, projectionMatrix, 0, mTempMvMatrix, 0)
  }

}