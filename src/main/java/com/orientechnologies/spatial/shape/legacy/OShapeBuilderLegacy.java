package com.orientechnologies.spatial.shape.legacy;

import com.orientechnologies.orient.core.index.OCompositeKey;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.shape.Shape;

/**
 * Created by Enrico Risa on 23/10/15.
 */
public interface OShapeBuilderLegacy<T extends Shape> {

  public T makeShape(OCompositeKey key, SpatialContext ctx);

  public boolean canHandle(OCompositeKey key);
}
