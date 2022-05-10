package com.vegeta.glstudy

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.vegeta.glstudy.databinding.ActivityMainBinding
import com.vegeta.glstudy.learn.HelloTriangle

class MainActivity : AppCompatActivity() {

  private val binding by lazy { ActivityMainBinding.inflate(LayoutInflater.from(this)) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(binding.root)
    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

    val clickListener = click@{ view: View ->
      val lp = FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT
      )
      val glSurfaceView: GLSurfaceView
//      glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
      when (view.id) {
        R.id.btnTriangle -> {
          glSurfaceView = GLSurfaceView(this).apply {
            layoutParams = lp
            setEGLContextClientVersion(3)
          }
          glSurfaceView.setRenderer(TriangleRenderer(this))
        }
        R.id.btnPic -> {
          glSurfaceView = PicGLSurfaceView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
              FrameLayout.LayoutParams.MATCH_PARENT,
              FrameLayout.LayoutParams.MATCH_PARENT
            )
            setEGLContextClientVersion(3)
          }
          glSurfaceView.setRenderer(PicRenderer(this))
        }
        R.id.btnSphere -> {
          glSurfaceView = SphereSurfaceView(this).apply {
            layoutParams = lp
            setEGLContextClientVersion(3)
          }
          glSurfaceView.setRenderer(SphereRenderer(this,"no local file"))
          glSurfaceView.renderMode= GLSurfaceView.RENDERMODE_WHEN_DIRTY
        }
        R.id.learnHelloTriangle -> {
          glSurfaceView = GLSurfaceView(this).apply {
            layoutParams = lp
            setEGLContextClientVersion(3)
          }
          glSurfaceView.setRenderer(HelloTriangle(this))
        }
        else -> return@click
      }
      binding.parent.addView(glSurfaceView)
    }

    binding.btnTriangle.setOnClickListener(clickListener)
    binding.btnPic.setOnClickListener(clickListener)
    binding.btnSphere.setOnClickListener(clickListener)
    binding.learnHelloTriangle.setOnClickListener(clickListener)

    // show default GLSurfaceView
    binding.btnSphere.performClick()
  }

  override fun onBackPressed() {
    val glSurfaceView = binding.parent.getChildAt(binding.parent.childCount - 1)
    if (glSurfaceView is GLSurfaceView) {
      // 点击返回键，退出GLSurfaceView页面
      binding.parent.removeView(glSurfaceView)
    } else {
      super.onBackPressed()
    }
  }

  override fun onResume() {
    super.onResume()
    val glSurfaceView = binding.parent.getChildAt(binding.parent.childCount - 1)
    if (glSurfaceView is GLSurfaceView) {
      glSurfaceView.onResume()
    }
  }

  override fun onPause() {
    super.onPause()
    val glSurfaceView = binding.parent.getChildAt(binding.parent.childCount - 1)
    if (glSurfaceView is GLSurfaceView) {
      glSurfaceView.onPause()
    }
  }


}
