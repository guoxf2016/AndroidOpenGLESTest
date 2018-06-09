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
import java.nio.ShortBuffer
import android.opengl.GLUtils
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.content.Context.ACTIVITY_SERVICE
import android.app.ActivityManager
import android.content.Context
import android.util.Log


class MainActivity : AppCompatActivity(), GLSurfaceView.Renderer {

    companion object {
        const val TAG = "MainActivity"
    }

    private val VERTEX_SHADER = (
            "attribute vec4 vPosition;\n"
                    + "uniform mat4 uMVPMatrix;\n"
                    + "attribute vec2 a_texCoord;\n"
                    + "varying vec2 v_texCoord;\n"
                    + "void main() {\n"
                    + " gl_Position = uMVPMatrix * vPosition;\n"
                    + " v_texCoord = a_texCoord;\n"
                    + "}")
    private val FRAGMENT_SHADER = (
            "precision mediump float;\n"
                    +"varying vec2 v_texCoord;\n"
                    +"uniform sampler2D s_texture;\n"
                    + "void main() {\n"
                    + " gl_FragColor = texture2D(s_texture, v_texCoord);\n"
                    + "}")
    private val VERTEX = floatArrayOf(// in counterclockwise order:
            1f, 1f, 0f,   // top right
            -1f, 1f, 0f,  // top left
            -1f, -1f, 0f, // bottom left
            1f, -1f, 0f // bottom right
    )
    private val floatBuffer: FloatBuffer by lazy {
        ByteBuffer.allocateDirect(VERTEX.size * 6)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(VERTEX)
    }

    private val VERTEX_INDEX = shortArrayOf(0, 1, 2, 0, 2, 3)

    private val vertexIndexBuffer: ShortBuffer by lazy {
        ByteBuffer.allocateDirect(VERTEX_INDEX.size * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(VERTEX_INDEX)
    }

    private val TEX_VERTEX = floatArrayOf(// in clockwise order:
            1f, 0f, // bottom right
            0f, 0f, // bottom left
            0f, 1f, // top left
            1f, 1f)// top right

    private val texIndexBuffer: FloatBuffer by lazy {
        ByteBuffer.allocateDirect(TEX_VERTEX.size * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(TEX_VERTEX)
    }

    private var mMVPMatrixHandle: Int? = null
    private var mTexCoordHandle: Int? = null
    private var mTexSamplerHandle: Int? = null

    private var mTexName: Int? = null

    private var mMVPMatrix: FloatArray = FloatArray(32)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val activityManager = this.getSystemService(
                Context.ACTIVITY_SERVICE) as ActivityManager
        if (activityManager.deviceConfigurationInfo.reqGlEsVersion < 0x20000) {
            Log.d(TAG, "gles 2.0 is not support!")
            finish()
            return
        }

        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        glSurfaceView.setRenderer(this)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        floatBuffer.position(0)
        vertexIndexBuffer.position(0)
        texIndexBuffer.position(0)
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
        GLES20.glUniform1i(mTexSamplerHandle!!, 0)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, VERTEX_INDEX.size,
                GLES20.GL_UNSIGNED_SHORT, vertexIndexBuffer)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        Matrix.perspectiveM(mMVPMatrix, 0, 45f, width.toFloat() / height, 0.1f, 100f)
        Matrix.translateM(mMVPMatrix, 0, 0f, 0f, -5f)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        val texNames = IntArray(1)
        GLES20.glGenTextures(1, texNames, 0)
        mTexName = texNames[0]
        val bitmap = BitmapFactory.decodeResource(this.resources,
                R.drawable.p_300px)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexName!!)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_REPEAT)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()

        val program = GLES20.glCreateProgram()
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)

        GLES20.glUseProgram(program)

        val positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        mMVPMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        mTexCoordHandle = GLES20.glGetAttribLocation(program, "a_texCoord")
        mTexSamplerHandle = GLES20.glGetUniformLocation(program, "s_texture")

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false,
                12, floatBuffer)

        GLES20.glEnableVertexAttribArray(mTexCoordHandle!!)
        GLES20.glVertexAttribPointer(mTexCoordHandle!!, 2, GLES20.GL_FLOAT, false, 0, texIndexBuffer)
    }

    fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }
}

