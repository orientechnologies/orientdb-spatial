package com.orientechnologies.spatial.shape.legacy;

import com.orientechnologies.orient.core.index.OCompositeKey;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.shape.Shape;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Enrico Risa on 23/10/15.
 */
public class OShapeBuilderLegacyImpl implements OShapeBuilderLegacy<Shape> {

  public static OShapeBuilderLegacyImpl INSTANCE = new OShapeBuilderLegacyImpl();
  List<OShapeBuilderLegacy> builders = new ArrayList<OShapeBuilderLegacy>();

  protected OShapeBuilderLegacyImpl() {
    builders.add(new OPointLegecyBuilder());
    builders.add(new ORectangleLegacyBuilder());
  }

  @Override
  public Shape makeShape(OCompositeKey key, SpatialContext ctx) {
    for (OShapeBuilderLegacy f : builders) {
      if (f.canHandle(key)) {
        return f.makeShape(key, ctx);
      }
    }
    return null;
  }

  @Override
  public boolean canHandle(OCompositeKey key) {
    return false;
  }
}
