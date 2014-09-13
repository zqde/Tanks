package com.ThirtyNineEighty.Renderable;

import android.opengl.GLES20;
import android.util.Log;

import com.ThirtyNineEighty.System.ActivityContext;

import java.io.IOException;
import java.io.InputStream;

public abstract class Shader
{
  protected static Shader current;
  private static Shader shader2D;
  private static Shader shader3D;

  public static Shader getCurrent()
  {
    return current;
  }

  public static void setShader3D()
  {
    if (shader3D == null)
    {
      shader3D = new Shader3D();
      shader3D.Compile();
    }

    if (current != shader3D)
    {
      current = shader3D;
      GLES20.glUseProgram(current.shaderProgramHandle);
    }
  }

  public static void setShader2D()
  {
    current = null;
  }

  protected int shaderProgramHandle;

  public abstract void Compile();
  protected abstract void GetLocations();

  protected void Compile(String vertexFileName, String fragmentFileName)
  {
    int vertexShader    = CompileShader(GLES20.GL_VERTEX_SHADER, vertexFileName);
    int fragmentShader  = CompileShader(GLES20.GL_FRAGMENT_SHADER, fragmentFileName);
    shaderProgramHandle = GLES20.glCreateProgram();

    GLES20.glAttachShader(shaderProgramHandle, vertexShader);
    GLES20.glAttachShader(shaderProgramHandle, fragmentShader);
    GLES20.glLinkProgram(shaderProgramHandle);

    GLES20.glReleaseShaderCompiler();

    GetLocations();
  }

  private int CompileShader(int type, String path)
  {
    int shaderHandle = 0;

    try
    {
      //load shader source
      shaderHandle = GLES20.glCreateShader(type);
      InputStream stream = ActivityContext.getContext().getAssets().open(path);
      int size = stream.available();
      byte[] buffer = new byte[size];
      int readCount = stream.read(buffer);

      if (readCount != size)
        Log.e("Error", "file read not fully: " + path);

      String source = new String(buffer);
      stream.close();

      //compile shader
      GLES20.glShaderSource(shaderHandle, source);
      GLES20.glCompileShader(shaderHandle);

      //check for errors
      int[] compiled = new int[1];
      GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compiled, 0);
      if (compiled[0] == 0)
      {
        Log.e("Error", GLES20.glGetShaderInfoLog(shaderHandle));
        GLES20.glDeleteShader(shaderHandle);
      }
    }
    catch(IOException e)
    {
      Log.e("Error", e.getMessage());
    }

    return shaderHandle;
  }
}