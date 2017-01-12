/*
 * Copyright 2014 Orient Technologies.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.orientechnologies.spatial.shape;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.spatial4j.core.shape.Point;
import com.spatial4j.core.shape.Rectangle;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.ShapeCollection;
import com.spatial4j.core.shape.jts.JtsGeometry;
import com.vividsolutions.jts.geom.Geometry;

public class OShapeFactory extends OComplexShapeBuilder {

  public static final OShapeFactory INSTANCE = new OShapeFactory();

  private Map<String, OShapeBuilder> factories = new HashMap<String, OShapeBuilder>();

  protected OShapeOperation operation;

  protected OShapeFactory() {
    operation = new OShapeOperationImpl(this);
    registerFactory(new OLineStringShapeBuilder());
    registerFactory(new OMultiLineStringShapeBuilder());
    registerFactory(new OPointShapeBuilder());
    registerFactory(new OMultiPointShapeBuilder());
    registerFactory(new ORectangleShapeBuilder());
    registerFactory(new OPolygonShapeBuilder());
    registerFactory(new OMultiPolygonShapeBuilder());
    registerFactory(new OGeometryCollectionShapeBuilder(this));
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public OShapeType getType() {
    return null;
  }

  @Override
  public void initClazz(ODatabaseDocumentTx db) {
    for (OShapeBuilder f : factories.values()) {
      f.initClazz(db);
    }
  }

  @Override
  public Shape fromDoc(ODocument document) {
    OShapeBuilder oShapeBuilder = factories.get(document.getClassName());
    if (oShapeBuilder != null) {
      return oShapeBuilder.fromDoc(document);
    }
    // TODO handle exception shape not found
    return null;
  }

  @Override
  public Shape fromObject(Object obj) {

    if (obj instanceof String) {
      try {
        return fromText((String) obj);
      } catch (ParseException e) {
        e.printStackTrace();
      }
    }
    if (obj instanceof ODocument) {
      return fromDoc((ODocument) obj);
    }
    if (obj instanceof Map) {
      Map map = (Map) ((Map) obj).get("shape");
      if (map == null) {
        map = (Map) obj;
      }
      return fromMapGeoJson(map);
    }
    return null;
  }

  @Override
  public String asText(ODocument document) {
    OShapeBuilder oShapeBuilder = factories.get(document.getClassName());
    if (oShapeBuilder != null) {
      return oShapeBuilder.asText(document);
    }
    // TODO handle exception shape not found
    return null;
  }

  @Override
  public String asText(Object obj) {

    if (obj instanceof ODocument) {
      return asText((ODocument) obj);
    }
    if (obj instanceof Map) {
      Map map = (Map) ((Map) obj).get("shape");
      if (map == null) {
        map = (Map) obj;
      }
      return asText(map);
    }
    return null;
  }

  public byte[] asBinary(Object obj) {

    if (obj instanceof ODocument) {
      Shape shape = fromDoc((ODocument) obj);
      return asBinary(shape);
    }
    if (obj instanceof Map) {
      Map map = (Map) ((Map) obj).get("shape");
      if (map == null) {
        map = (Map) obj;
      }
      Shape shape = fromMapGeoJson(map);

      return asBinary(shape);
    }
    throw new IllegalArgumentException("Error serializing to binary " + obj);
  }

  @Override
  public ODocument toDoc(Shape shape) {

    // TODO REFACTOR
    ODocument doc = null;
    if (Point.class.isAssignableFrom(shape.getClass())) {
      doc = factories.get(OPointShapeBuilder.NAME).toDoc(shape);
    } else if (Rectangle.class.isAssignableFrom(shape.getClass())) {
      doc = factories.get(ORectangleShapeBuilder.NAME).toDoc(shape);
    } else if (JtsGeometry.class.isAssignableFrom(shape.getClass())) {
      JtsGeometry geometry = (JtsGeometry) shape;
      Geometry geom = geometry.getGeom();
      doc = factories.get("O" + geom.getClass().getSimpleName()).toDoc(shape);

    } else if (ShapeCollection.class.isAssignableFrom(shape.getClass())) {
      ShapeCollection collection = (ShapeCollection) shape;

      if (isMultiPolygon(collection)) {
        doc = factories.get("OMultiPolygon").toDoc(createMultiPolygon(collection));
      } else if (isMultiPoint(collection)) {
        doc = factories.get("OMultiPoint").toDoc(createMultiPoint(collection));
      } else if (isMultiLine(collection)) {
        doc = factories.get("OMultiLineString").toDoc(createMultiLine(collection));
      } else {
        doc = factories.get("OGeometryCollection").toDoc(shape);
      }
    }
    return doc;
  }

  @Override
  public Shape fromMapGeoJson(Map geoJsonMap) {
    OShapeBuilder oShapeBuilder = factories.get(geoJsonMap.get("type"));

    if (oShapeBuilder == null) {
      oShapeBuilder = factories.get(geoJsonMap.get("@class"));
    }
    if (oShapeBuilder != null) {
      return oShapeBuilder.fromMapGeoJson(geoJsonMap);
    }
    throw new IllegalArgumentException("Invalid map");
    // TODO handle exception shape not found
  }

  public Geometry toGeometry(Shape shape) {
	if(shape instanceof ShapeCollection){
		ShapeCollection<Shape> shapes = (ShapeCollection<Shape>) shape;
		Geometry[] geometries = new Geometry[shapes.size()];
	    int i = 0;
	    for (Shape shapeItem : shapes) {
	      geometries[i] = SPATIAL_CONTEXT.getGeometryFrom(shapeItem);
	      i++;
	    }
	    return GEOMETRY_FACTORY.createGeometryCollection(geometries);
	} else {
		return SPATIAL_CONTEXT.getGeometryFrom(shape);
	}
  }

  public ODocument toDoc(Geometry geometry) {
	  if (geometry instanceof com.vividsolutions.jts.geom.Point) {
	      com.vividsolutions.jts.geom.Point point = (com.vividsolutions.jts.geom.Point) geometry;
	      Point point1 = context().makePoint(point.getX(), point.getY());
	      return toDoc(point1);
	    }
	    if(geometry instanceof com.vividsolutions.jts.geom.GeometryCollection){
	    	com.vividsolutions.jts.geom.GeometryCollection gc = (com.vividsolutions.jts.geom.GeometryCollection) geometry;
	    	List<Shape> shapes = new ArrayList<Shape>();
	    	for(int i = 0; i < gc.getNumGeometries(); i++){
	    		Geometry geo = gc.getGeometryN(i);
	    		Shape shape = SPATIAL_CONTEXT.makeShape(geo);
	    		shapes.add(shape);
	    	}
	    	return toDoc(new ShapeCollection<Shape>(shapes, SPATIAL_CONTEXT));
	    }
	    return toDoc(SPATIAL_CONTEXT.makeShape(geometry));
  }

  public OShapeOperation operation() {
    return operation;
  }

  public void registerFactory(OShapeBuilder factory) {
    factories.put(factory.getName(), factory);
  }
}
