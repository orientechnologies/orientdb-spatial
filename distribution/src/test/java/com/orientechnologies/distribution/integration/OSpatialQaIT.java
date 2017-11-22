package com.orientechnologies.distribution.integration;

import com.orientechnologies.orient.core.sql.executor.OResultSet;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by frank on 17/03/2017.
 */
@Test
public class OSpatialQaIT extends OIntegrationTestTemplate {

  @Test
  public void testSpatial() throws Exception {
    OResultSet resultSet;

    resultSet = db.query(
        "SELECT from ArchaeologicalSites where ST_Within(Location, ST_Buffer(ST_GeomFromText('POINT(41.8898464 12.4866727 )'), 50)) = true");

    Assert.assertEquals(resultSet.stream().count(), 55);
    resultSet.close();
  }
}
