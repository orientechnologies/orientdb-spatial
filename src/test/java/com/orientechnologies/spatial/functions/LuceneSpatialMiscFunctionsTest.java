/*
 *
 *  * Copyright 2014 Orient Technologies.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  
 */

package com.orientechnologies.spatial.functions;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.spatial.shape.OShapeFactory;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.jts.JtsGeometry;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import com.vividsolutions.jts.geom.Polygon;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.util.List;

/**
 * Created by Enrico Risa on 28/09/15.
 */
@Test(groups = "embedded")
public class LuceneSpatialMiscFunctionsTest {

  @Test
  public void testStEquals() {

    OrientGraphNoTx graph = new OrientGraphNoTx("memory:functionsTest");
    try {
      ODatabaseDocumentTx db = graph.getRawGraph();

      List<ODocument> execute = db.command(
          new OCommandSQL(
              "SELECT ST_Equals(ST_GeomFromText('LINESTRING(0 0, 10 10)'), ST_GeomFromText('LINESTRING(0 0, 5 5, 10 10)'))"))
          .execute();
      ODocument next = execute.iterator().next();
      Assert.assertEquals(next.field("ST_Equals"), true);
    } finally {
      graph.drop();
    }
  }

  // TODO reanable and check byte[]
  @Test(enabled = false)
  public void testAsBinary() {

    OrientGraphNoTx graph = new OrientGraphNoTx("memory:functionsTest");
    try {
      ODatabaseDocumentTx db = graph.getRawGraph();

      List<ODocument> execute = db.command(new OCommandSQL("SELECT ST_AsBinary(ST_GeomFromText('LINESTRING(0 0, 10 10)'))"))
          .execute();
      ODocument next = execute.iterator().next();
      // TODO CHANGE
      Assert.assertNull(next.field("ST_AsBinary"));
    } finally {
      graph.drop();
    }
  }

  @Test
  public void testEnvelope() {

    OrientGraphNoTx graph = new OrientGraphNoTx("memory:functionsTest");
    try {
      ODatabaseDocumentTx db = graph.getRawGraph();

      List<ODocument> execute = db.command(new OCommandSQL("SELECT ST_AsText(ST_Envelope('LINESTRING(0 0, 1 3)'))")).execute();
      ODocument next = execute.iterator().next();
      Assert.assertEquals(next.field("ST_AsText"), "POLYGON ((0 0, 0 3, 1 3, 1 0, 0 0))");

    } finally {
      graph.drop();
    }
  }

  @Test
  public void testBuffer() {

    OrientGraphNoTx graph = new OrientGraphNoTx("memory:functionsTest");
    try {
      ODatabaseDocumentTx db = graph.getRawGraph();

      List<ODocument> execute = db.command(new OCommandSQL("SELECT ST_AsText(ST_Buffer(ST_GeomFromText('POINT(100 90)'),50));"))
          .execute();
      ODocument next = execute.iterator().next();
      Assert
          .assertEquals(
              next.field("ST_AsText"),
              "POLYGON ((150 90, 149.0392640201615 80.24548389919359, 146.19397662556435 70.86582838174552, 141.57348061512727 62.22148834901989, 135.35533905932738 54.64466094067263, 127.77851165098011 48.42651938487274, 119.1341716182545 43.80602337443566, 109.75451610080641 40.960735979838475, 100 40, 90.24548389919359 40.960735979838475, 80.86582838174552 43.80602337443566, 72.2214883490199 48.426519384872734, 64.64466094067262 54.64466094067262, 58.426519384872734 62.22148834901989, 53.80602337443566 70.86582838174553, 50.960735979838475 80.24548389919362, 50 90.00000000000004, 50.96073597983849 99.75451610080646, 53.80602337443568 109.13417161825454, 58.426519384872776 117.77851165098016, 64.64466094067268 125.35533905932743, 72.22148834901996 131.57348061512732, 80.8658283817456 136.19397662556437, 90.2454838991937 139.03926402016154, 100.00000000000013 140, 109.75451610080654 139.0392640201615, 119.13417161825463 136.1939766255643, 127.77851165098025 131.57348061512718, 135.3553390593275 125.35533905932726, 141.57348061512735 117.77851165097996, 146.1939766255644 109.13417161825431, 149.03926402016157 99.75451610080621, 150 90))");

      execute = db.command(new OCommandSQL("SELECT ST_AsText(ST_Buffer(ST_GeomFromText('POINT(100 90)'), 50, { quadSegs : 2 }));"))
          .execute();
      next = execute.iterator().next();

      Assert
          .assertEquals(
              next.field("ST_AsText"),
              "POLYGON ((150 90, 135.35533905932738 54.64466094067263, 100 40, 64.64466094067262 54.64466094067262, 50 90, 64.64466094067262 125.35533905932738, 99.99999999999999 140, 135.35533905932738 125.35533905932738, 150 90))");

      execute = db.command(
          new OCommandSQL(
              "SELECT ST_AsText(ST_Buffer(ST_GeomFromText('LINESTRING(0 0,75 75,75 0)'), 10, { 'endCap' : 'square' }));"))
          .execute();
      next = execute.iterator().next();
      Assert
          .assertEquals(
              next.field("ST_AsText"),
              "POLYGON ((67.92893218813452 82.07106781186548, 69.44429766980397 83.31469612302546, 71.1731656763491 84.23879532511287, 73.04909677983872 84.80785280403231, 75 85, 76.95090322016128 84.80785280403231, 78.8268343236509 84.23879532511287, 80.55570233019603 83.31469612302546, 82.07106781186548 82.07106781186548, 83.31469612302546 80.55570233019603, 84.23879532511287 78.8268343236509, 84.80785280403231 76.95090322016128, 85 75, 85 0, 84.80785280403231 -1.9509032201612824, 84.23879532511287 -3.826834323650898, 83.31469612302546 -5.555702330196022, 82.07106781186548 -7.071067811865475, 80.55570233019603 -8.314696123025453, 78.8268343236509 -9.238795325112868, 76.95090322016128 -9.807852804032304, 75 -10, 73.04909677983872 -9.807852804032304, 71.1731656763491 -9.238795325112868, 69.44429766980397 -8.314696123025453, 67.92893218813452 -7.0710678118654755, 66.68530387697454 -5.555702330196022, 65.76120467488713 -3.8268343236508944, 65.19214719596769 -1.9509032201612773, 65 0, 65 50.85786437626905, 7.071067811865475 -7.071067811865475, 5.555702330196023 -8.314696123025453, 3.8268343236508984 -9.238795325112868, 1.9509032201612833 -9.807852804032304, 0.0000000000000006 -10, -1.950903220161282 -9.807852804032304, -3.826834323650897 -9.238795325112868, -5.55570233019602 -8.314696123025453, -7.071067811865475 -7.0710678118654755, -8.314696123025453 -5.555702330196022, -9.238795325112868 -3.826834323650899, -9.807852804032304 -1.9509032201612861, -10 -0.0000000000000012, -9.807852804032304 1.9509032201612837, -9.238795325112866 3.8268343236509006, -8.314696123025449 5.555702330196026, -7.071067811865475 7.071067811865475, 67.92893218813452 82.07106781186548))");

    } finally {
      graph.drop();
    }
  }

  // todo check distance
  @Test
  public void testDistance() {

    OrientGraphNoTx graph = new OrientGraphNoTx("memory:functionsTest");
    try {
      ODatabaseDocumentTx db = graph.getRawGraph();

      List<ODocument> execute = db
          .command(
              new OCommandSQL(
                  "SELECT ST_Distance(ST_GeomFromText('POINT(-72.1235 42.3521)'),ST_GeomFromText('LINESTRING(-72.1260 42.45, -72.123 42.1546)'))"))
          .execute();
      ODocument next = execute.iterator().next();

      Assert.assertEquals(next.field("ST_Distance"), 0.0015056772638228177);

      execute = db
          .command(
              new OCommandSQL(
                  "SELECT  ST_Distance( ST_GeomFromText('LINESTRING(13.45 52.47,13.46 52.48)'), ST_GeomFromText('LINESTRING(13.00 52.00,13.1 52.2)'))"))
          .execute();
      next = execute.iterator().next();

      Assert.assertEquals(next.field("ST_Distance"), 0.44204072210600415);
    } finally {
      graph.drop();
    }

  }

  @Test
  public void testDisjoint() {

    OrientGraphNoTx graph = new OrientGraphNoTx("memory:functionsTest");
    try {
      ODatabaseDocumentTx db = graph.getRawGraph();

      List<ODocument> execute = db.command(new OCommandSQL("SELECT ST_Disjoint('POINT(0 0)', 'LINESTRING ( 2 0, 0 2 )');"))
          .execute();
      ODocument next = execute.iterator().next();

      Assert.assertEquals(next.field("ST_Disjoint"), true);

      execute = db.command(new OCommandSQL("SELECT ST_Disjoint('POINT(0 0)', 'LINESTRING ( 0 0, 0 2 )');")).execute();
      next = execute.iterator().next();

      Assert.assertEquals(next.field("ST_Disjoint"), false);
    } finally {
      graph.drop();
    }
  }

  @Test
  public void testWktPolygon() throws ParseException {

    Shape shape = OShapeFactory.INSTANCE.fromObject("POLYGON((0 0, 10 0, 10 5, 0 5, 0 0))");

    Assert.assertEquals(shape instanceof JtsGeometry, true);

    JtsGeometry geom = (JtsGeometry) shape;
    Assert.assertEquals(geom.getGeom() instanceof Polygon, true);
  }
}
