package tech.kaidevrim.khip_8

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class OpenGLView(context: Context, attrs: AttributeSet) : GLSurfaceView(context, attrs) {
    private val renderer: OpenGLRenderer
    init {
        setEGLContextClientVersion(2)
        renderer = OpenGLRenderer()
        setRenderer(renderer)
    }
}