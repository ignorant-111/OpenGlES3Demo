package com.fengao.opengles3demo.util

import android.content.Context
import android.content.res.Resources
import android.opengl.GLES30
import android.util.Log
import java.io.ByteArrayOutputStream

object ShaderUtil {
    fun loadShader(
        shaderType: Int,
        source: String?
    ): Int {
        var shader = GLES30.glCreateShader(shaderType)
        if (shader != 0) {
            GLES30.glShaderSource(shader, source)
            GLES30.glCompileShader(shader)
            checkGLError("glCompileShader")
            val compiled = IntArray(1)
            GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                Log.e("ES20_ERROR", "Could not compile shader $shaderType:")
                Log.e("ES20_ERROR", "ERROR: " + GLES30.glGetShaderInfoLog(shader))
                GLES30.glDeleteShader(shader)
                shader = 0
            }
        } else {
            Log.e(
                "ES20_ERROR", "Could not Create shader " + shaderType + ":" +
                        "Error:" + shader
            )
        }
        return shader
    }

    fun loadProgramFromAssets(
        VShaderName: String?,
        FShaderName: String?,
        resources: Resources
    ): Int {
        val vertexText = loadFromAssetsFile(VShaderName, resources)
        val fragmentText = loadFromAssetsFile(FShaderName, resources)
        return createProgram(vertexText, fragmentText)
    }


    fun checkGLError(op: String) {
        var error: Int
        while (GLES30.glGetError().also { error = it } != GLES30.GL_NO_ERROR) {
            Log.e("ES20_ERROR", "$op: glError $error")
            throw RuntimeException("$op: glError $error")
        }
    }

    fun createProgram(
        vertexSource: String?,
        fragmentSource: String?
    ): Int {
        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == 0) {
            return 0
        }
        val fragShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource)
        if (fragShader == 0) {
            return 0
        }
        var program = GLES30.glCreateProgram()
        if (program != 0) {
            GLES30.glAttachShader(program, vertexShader)
            checkGLError("glAttachShader")
            GLES30.glAttachShader(program, fragShader)
            checkGLError("glAttachShader")
            GLES30.glLinkProgram(program)
            val linkStatus = IntArray(1)
            GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] != GLES30.GL_TRUE) {
                GLES30.glDeleteProgram(program)
                program = 0
            }
        } else {
            Log.e("ES20_ERROR", "glCreateProgram Failed: $program")
        }
        return program
    }

    fun loadFromAssetsFile(
        fileName: String?,
        resources: Resources
    ): String? {
        var result: String? = null
        try {
            val inputStream = resources.assets.open(fileName!!)
            var ch = 0
            val baos = ByteArrayOutputStream()
            while (inputStream.read().also { ch = it } != -1) {
                baos.write(ch)
            }
            val buffer = baos.toByteArray()
            baos.close()
            inputStream.close()
            result = String(buffer)
            result = result.replace("\\r\\n".toRegex(), "\n")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }


    fun compileShader(name: String?, type: Int, context: Context): Int {
        val shaderStr = loadFromAssetsFile(name, context.resources)
        val shaderId = GLES30.glCreateShader(type)
        if (shaderId != 0) { //load
            GLES30.glShaderSource(shaderId, shaderStr)
            GLES30.glCompileShader(shaderId)
            val compileStatus = IntArray(1)
            GLES30.glGetShaderiv(shaderId, GLES30.GL_COMPILE_STATUS, compileStatus, 0)
            if (compileStatus[0] == 0) {
                val logInfo = GLES30.glGetShaderInfoLog(shaderId)
                System.err.println(logInfo)
                GLES30.glDeleteShader(shaderId)
                return 0
            }
            return shaderId
        }
        return 0
    }

    fun linkProgram(vertexShaderId: Int, fragmentShaderId: Int): Int {
        val programId = GLES30.glCreateProgram()
        return if (programId != 0) {
            GLES30.glAttachShader(programId, vertexShaderId)
            GLES30.glAttachShader(programId, fragmentShaderId)
            GLES30.glLinkProgram(programId)
            val linkStatus = IntArray(1)
            GLES30.glGetProgramiv(programId, GLES30.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] == 0) {
                val logInfo = GLES30.glGetProgramInfoLog(programId)
                System.err.println(logInfo)
                GLES30.glDeleteProgram(programId)
                return 0
            }
            programId
        } else {
            0
        }
    }
}