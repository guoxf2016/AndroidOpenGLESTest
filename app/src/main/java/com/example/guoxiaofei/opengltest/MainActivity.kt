package com.example.guoxiaofei.opengltest

import android.opengl.GLSurfaceView
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import android.opengl.GLES20
import android.opengl.Matrix




class MainActivity : AppCompatActivity(), GLSurfaceView.Renderer {


    private val VERTEX_SHADER = (
            "attribute vec4 vPosition;\n"
                    + "uniform mat4 uMVPMatrix;\n"
                    + "void main() {\n"
                    + " gl_Position = uMVPMatrix * vPosition;\n"
                    + "}")
    private val FRAGMENT_SHADER = (
            "precision mediump float;\n"
                    + "void main() {\n"
                    + " gl_FragColor = vec4(0.5, 0, 0, 1);\n"
                    + "}")
    private val VERTEX = floatArrayOf(// in counterclockwise order:
            0f, 1f, 0f, // top
            -0.5f, -1f, 0f, // bottom left
            1f, -1f, 0f)// bottom right

    private val floatBuffer: FloatBuffer by lazy {
        ByteBuffer.allocateDirect(VERTEX.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(VERTEX)
    }

    private var mMVPMatrixHandle : Int? = null

    private var mMVPMatrix: FloatArray = FloatArray(32)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        glSurfaceView.setRenderer(this)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        floatBuffer.position(0)
        //mMVPMatrix.fill(1f, 0 , 32)
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
    }


    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle!!, 1, false, mMVPMatrix, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        Matrix.perspectiveM(mMVPMatrix, 0, 45f, width.toFloat() / height, 0.1f, 100f)
        Matrix.translateM(mMVPMatrix, 0, 0f, 0f, -2.5f)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        val program = GLES20.glCreateProgram()
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)

        GLES20.glUseProgram(program)

        val positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        mMVPMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false,
                12, floatBuffer)
    }

    fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }
}

