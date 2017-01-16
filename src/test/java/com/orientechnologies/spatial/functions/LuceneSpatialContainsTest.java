/**
 * Copyright 2010-2016 OrientDB LTD (http://orientdb.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * For more information: http://www.orientdb.com
 */
package com.orientechnologies.spatial.functions;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Created by Enrico Risa on 28/09/15.
 */
public class LuceneSpatialContainsTest {

  @Test
  public void testContainsNoIndex() {

    OrientGraphNoTx graph = new OrientGraphNoTx("memory:functionsTestNoIndex");
    try {
      ODatabaseDocumentTx db = graph.getRawGraph();

      List<ODocument> execute = db.command(new OCommandSQL(
          "select ST_Contains(smallc,smallc) as smallinsmall,ST_Contains(smallc, bigc) As smallinbig, ST_Contains(bigc,smallc) As biginsmall from (SELECT ST_Buffer(ST_GeomFromText('POINT(50 50)'), 20) As smallc,ST_Buffer(ST_GeomFromText('POINT(50 50)'), 40) As bigc)"))
          .execute();
      ODocument next = execute.iterator().next();

      Assert.assertEquals(next.field("smallinsmall"), true);
      Assert.assertEquals(next.field("smallinbig"), false);
      Assert.assertEquals(next.field("biginsmall"), true);

    } finally {
      graph.drop();
    }
  }

  @Test
  public void testContainsIndex() {

    OrientGraphNoTx graph = new OrientGraphNoTx("memory:functionsTestWithIndex");
    try {
      ODatabaseDocumentTx db = graph.getRawGraph();

      db.command(new OCommandSQL("create class Polygon extends v")).execute();
      db.command(new OCommandSQL("create property Polygon.geometry EMBEDDED OPolygon")).execute();

      db.command(new OCommandSQL("insert into Polygon set geometry = ST_Buffer(ST_GeomFromText('POINT(50 50)'), 20)")).execute();
      db.command(new OCommandSQL("insert into Polygon set geometry = ST_Buffer(ST_GeomFromText('POINT(50 50)'), 40)")).execute();

      db.command(new OCommandSQL("create index Polygon.g on Polygon (geometry) SPATIAL engine lucene")).execute();
      List<ODocument> execute = db
          .command(new OCommandSQL("SELECT from Polygon where ST_Contains(geometry, 'POINT(50 50)') = true")).execute();

      Assert.assertEquals(execute.size(), 2);

      execute = db.command(
          new OCommandSQL("SELECT from Polygon where ST_Contains(geometry, ST_Buffer(ST_GeomFromText('POINT(50 50)'), 30)) = true"))
          .execute();

      Assert.assertEquals(execute.size(), 1);

    } finally {
      graph.drop();
    }
  }
  
  @Test
  public void testContainsIndex_GeometryCollection() {

    OrientGraphNoTx graph = new OrientGraphNoTx("memory:functionsTestWithIndex");
    try {
      ODatabaseDocumentTx db = graph.getRawGraph();

      db.command(new OCommandSQL("create class Test extends v")).execute();
		db.command(new OCommandSQL("create property Test.geometry EMBEDDED OGeometryCollection")).execute();

		db.command(new OCommandSQL(
				"insert into Test set geometry = {'@type':'d','@class':'OGeometryCollection','geometries':[{'@type':'d','@class':'OPolygon','coordinates':[[[0,0],[10,0],[10,10],[0,10],[0,0]]]}]}"))
				.execute();
		db.command(new OCommandSQL(
				"insert into Test set geometry = {'@type':'d','@class':'OGeometryCollection','geometries':[{'@type':'d','@class':'OPolygon','coordinates':[[[11,11],[21,11],[21,21],[11,21],[11,11]]]}]}"))
				.execute();

		db.command(new OCommandSQL("create index Test.geometry on Test (geometry) SPATIAL engine lucene"))
				.execute();
		
		String testGeometry = "{'@type':'d','@class':'OGeometryCollection','geometries':[{'@type':'d','@class':'OPolygon','coordinates':[[[1,1],[2,1],[2,2],[1,2],[1,1]]]}]}";
		List<ODocument> execute = db
				.command(new OCommandSQL(
						"SELECT from Test where ST_Contains(geometry, " + testGeometry + ") = true"))
				.execute();

		Assert.assertEquals(execute.size(), 1);

    } finally {
      graph.drop();
    }
  }

}
