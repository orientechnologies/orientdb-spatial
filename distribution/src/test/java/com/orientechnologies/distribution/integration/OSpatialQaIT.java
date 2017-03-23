package com.orientechnologies.distribution.integration;

import com.orientechnologies.orient.core.sql.executor.OResultSet;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by frank on 17/03/2017.
 */
public class OSpatialQaIT extends OIntegrationTestTemplate {

  @Test
  public void testSpatial() throws Exception {
    OResultSet resultSet;

    resultSet = db.query(
        "SELECT from ArchaeologicalSites where ST_Within(Location, ST_Buffer(ST_GeomFromText('POINT(41.8898464 12.4866727 )'), 50)) = true");

//
    assertThat(resultSet).hasSize(55);

  }
}
