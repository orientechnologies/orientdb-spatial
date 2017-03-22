package com.orientechnologies.distribution.integration;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by frank on 17/03/2017.
 */
public class OSpatialQaIT extends OIntegrationTestTemplate {

  @Test
  public void testSpatial() throws Exception {

//    OResultSet resultSet = db.query(
//        "select * from ArchaeologicalSites where  ST_WITHIN(location,ST_Buffer(ST_GeomFromText('POINT(13.4115753 52.539275 )'), 1)) = true");
//
//    assertThat(resultSet).hasSize(1);

    List<?> list = db.query(new OSQLSynchQuery<ODocument>(
        "select * from ArchaeologicalSites where  ST_WITHIN(location,ST_Buffer(ST_GeomFromText('POINT(12.5113300 41.8919300  )'), 2)) = true"));

    assertThat(list).hasSize(1);

  }
}
