package com.ThirtyNineEighty.Renderable;

import android.opengl.GLES20;

public class Shader2D
  extends Shader
{
  public int attributePositionHandle;
  public int attributeTexCoordHandle;
  public int uniformMatrixHandle;
  public int uniformTextureHandle;

  @Override
  public void compile()
  {
    compile("Shaders/vertex2D.c", "Shaders/fragment2D.c");
  }

  @Override
  protected void getLocations()
  {
    //get shaders handles
    attributePositionHandle = GLES20.glGetAttribLocation(shaderProgramHandle, "a_position");
    attributeTexCoordHandle = GLES20.glGetAttribLocation(shaderProgramHandle, "a_texcoord");
    uniformMatrixHandle     = GLES20.glGetUniformLocation(shaderProgramHandle, "u_matrix");
    uniformTextureHandle    = GLES20.glGetUniformLocation(shaderProgramHandle, "u_texture");
  }
}
