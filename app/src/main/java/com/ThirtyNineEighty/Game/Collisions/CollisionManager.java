package com.ThirtyNineEighty.Game.Collisions;

import android.util.Log;

import com.ThirtyNineEighty.Game.Gameplay.Characteristics.Characteristic;
import com.ThirtyNineEighty.Game.Gameplay.GameObject;
import com.ThirtyNineEighty.Game.IEngineObject;
import com.ThirtyNineEighty.Helpers.Vector3;
import com.ThirtyNineEighty.System.GameContext;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CollisionManager
{
  private final Iterable<IEngineObject> worldObjects;
  private final ArrayList<IEngineObject> resolvingObjects;

  private final ExecutorService threadPool = Executors.newCachedThreadPool();

  public CollisionManager(Iterable<IEngineObject> objects)
  {
    worldObjects = objects;
    resolvingObjects = new ArrayList<IEngineObject>();
  }

  public void move(GameObject object)
  {
    Characteristic c = object.getCharacteristics();
    object.onMoved(c.getSpeed() * GameContext.getDelta());
    addToResolving(object);
  }

  public void move(IEngineObject object, float length)
  {
    object.onMoved(length);
    addToResolving(object);
  }

  public void move(IEngineObject object, Vector3 vector, float length)
  {
    object.onMoved(length, vector);
    addToResolving(object);
  }

  public void rotate(IEngineObject object, Vector3 angles)
  {
    object.onRotates(angles);
    addToResolving(object);
  }

  public void resolve()
  {
    ArrayList<Future<ResolveResult>> results = new ArrayList<Future<ResolveResult>>(resolvingObjects.size());

    for (final IEngineObject current : resolvingObjects)
    {
      Future<ResolveResult> futureResult = threadPool.submit(
        new Callable<ResolveResult>()
        {
          @Override
          public ResolveResult call() throws Exception
          {
            return resolve(current);
          }
        }
      );

      results.add(futureResult);
    }

    try
    {
      int size = results.size();
      for (int i = size - 1; i >= 0; i--)
      {
        Future<ResolveResult> current = results.get(i);

        ResolveResult result = current.get();
        if (result == null)
          continue;

        IEngineObject object = result.checkedObject;
        for (CollisionResult collResult : result.collisions)
        {
          Collision3D collision = collResult.collision;

          object.onMoved(collision.getMTVLength(), collision.getMTV());
          object.onCollide(collResult.collidedObject);
        }
      }
    }
    catch (Exception e)
    {
      Log.e("CollisionManager", "Resolve error", e);
    }

    resolvingObjects.clear();
  }

  private void addToResolving(IEngineObject object)
  {
    if (!resolvingObjects.contains(object))
      resolvingObjects.add(object);
  }

  private ResolveResult resolve(IEngineObject object)
  {
    ICollidable objectPh = object.getCollidable();
    if (objectPh == null)
      return null;

    ResolveResult result = null;

    for (IEngineObject current : worldObjects)
    {
      if (object == current)
        continue;

      ICollidable currentPh = current.getCollidable();
      if (currentPh == null)
        continue;

      if (objectPh.getRadius() + currentPh.getRadius() < getLength(object, current))
        continue;

      Collision3D collision = new Collision3D(objectPh, currentPh);

      if (collision.isCollide())
      {
        if (result == null)
          result = new ResolveResult(object);

        result.collisions.add(new CollisionResult(current, collision));
      }
    }

    return result;
  }

  private float getLength(IEngineObject one, IEngineObject two)
  {
    Vector3 positionOne = one.getPosition();
    Vector3 positionTwo = two.getPosition();

    Vector3 lengthVector = positionOne.getSubtract(positionTwo);
    return lengthVector.getLength();
  }

  private static class ResolveResult
  {
    public final IEngineObject checkedObject;
    public final LinkedList<CollisionResult> collisions;

    public ResolveResult(IEngineObject obj)
    {
      checkedObject = obj;
      collisions = new LinkedList<CollisionResult>();
    }
  }

  private static class CollisionResult
  {
    public final IEngineObject collidedObject;
    public final Collision3D collision;

    public CollisionResult(IEngineObject obj, Collision3D coll)
    {
      collidedObject = obj;
      collision = coll;
    }
  }
}
