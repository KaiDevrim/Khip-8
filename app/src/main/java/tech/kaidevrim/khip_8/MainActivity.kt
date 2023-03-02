package tech.kaidevrim.khip_8

import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle

class MainActivity : Activity() {
    private lateinit var gLView: GLSurfaceView
    private lateinit var chip8: Chip8
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        gLView = findViewById<GLSurfaceView>(R.id.openGLView)
        chip8.init(chip8)

    }

    override fun onResume() {
        super.onResume()
        gLView.onResume()
    }

    override fun onPause() {
        super.onPause()
        gLView.onPause()
    }
}