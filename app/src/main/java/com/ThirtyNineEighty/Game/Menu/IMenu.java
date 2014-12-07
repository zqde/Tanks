package com.ThirtyNineEighty.Game.Menu;

import android.view.MotionEvent;

import com.ThirtyNineEighty.Renderable.Renderable2D.I2DRenderable;

import java.util.List;

public interface IMenu
{
  void fillRenderable(List<I2DRenderable> renderables);
  boolean processEvent(MotionEvent event);
}
