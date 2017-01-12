package com.orientechnologies.spatial;

import java.util.List;

import org.junit.Test;
import org.junit.Assert;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;

public class GeometryCollectionTest extends BaseSpatialLuceneTest {

	@Test
	public void testDeleteVerticesWithGeometryCollection() {
		db.command(new OCommandSQL("CREATE CLASS Test extends V")).execute();
		db.command(new OCommandSQL("CREATE PROPERTY Test.name STRING")).execute();
		db.command(new OCommandSQL("CREATE PROPERTY Test.geometry EMBEDDED OGeometryCollection")).execute();

		db.command(new OCommandSQL("CREATE INDEX Test.geometry ON Test(geometry) SPATIAL ENGINE LUCENE")).execute();

		db.command(new OCommandSQL(
				"insert into Test content {'name': 'loc1', 'geometry': {'@type':'d','@class':'OGeometryCollection','geometries':[{'@type':'d','@class':'OPolygon','coordinates':[[[0,0],[0,10],[10,10],[10,0],[0,0]]]}]}}"))
				.execute();
		db.command(new OCommandSQL(
				"insert into Test content {'name': 'loc2', 'geometry': {'@type':'d','@class':'OGeometryCollection','geometries':[{'@type':'d','@class':'OPolygon','coordinates':[[[0,0],[0,20],[20,20],[20,0],[0,0]]]}]}}"))
				.execute();

		List<ODocument> qResult = db
				.command(new OCommandSQL(
						"select * from Test where ST_WITHIN(geometry,'POLYGON ((0 0, 15 0, 15 15, 0 15, 0 0))') = true"))
				.execute();
		Assert.assertEquals(1, qResult.size());

		db.command(new OCommandSQL("DELETE VERTEX Test")).execute();
		
		List<ODocument> qResult2 = db.command(new OCommandSQL("select * from Test")).execute();
		Assert.assertEquals(0, qResult2.size());
	}

}
