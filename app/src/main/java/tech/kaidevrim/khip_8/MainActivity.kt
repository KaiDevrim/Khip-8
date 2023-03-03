package tech.kaidevrim.khip_8

import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle
import java.lang.Exception
import kotlin.system.exitProcess

class MainActivity : Activity() {
    private lateinit var gLView: GLSurfaceView
    private var chip8: Chip8 = Chip8()
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        gLView = findViewById<GLSurfaceView>(R.id.openGLView)
        chip8.init(chip8)
        var keepOpen = true
        while (keepOpen) {
            try {
                chip8.cycle(chip8)
            } catch (err: Exception) {
                println(err)
                exitProcess(-1)
            }
            keepOpen = false
        }
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