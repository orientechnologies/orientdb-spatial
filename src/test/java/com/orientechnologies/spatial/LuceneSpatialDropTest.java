package com.orientechnologies.spatial;

import com.orientechnologies.orient.core.db.OPartitionedDatabasePool;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * Created by frank on 15/12/2016.
 */
public class LuceneSpatialDropTest {

  @Test
  public void testDeleteLuceneIndex1() {

    String dbName = "plocal:./playground/db/db.2.2.13";

    int INSERTCOUNT = 100; // @maggiolo00 set cont to 0 and the test will not fail anymore

    ODatabaseDocumentTx db = new ODatabaseDocumentTx(dbName);

    db.create();
    OClass test = db.getMetadata().getSchema().createClass("test");
    test.createProperty("name", OType.STRING);
    test.createProperty("latitude", OType.DOUBLE).setMandatory(false);
    test.createProperty("longitude", OType.DOUBLE).setMandatory(false);
    db.command(new OCommandSQL("create index test.name on test (name) FULLTEXT ENGINE LUCENE")).execute();
    db.command(new OCommandSQL("create index test.ll on test (latitude,longitude) SPATIAL ENGINE LUCENE")).execute();
    db.close();

    OPartitionedDatabasePool dbPool = new OPartitionedDatabasePool(dbName, "admin", "admin");

    db = dbPool.acquire();
    fillDb(db, INSERTCOUNT);
    db.close();

    db = dbPool.acquire();
    // @maggiolo00 Remove the next three lines and the test will not fail anymore
    OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>(
        "select from test where [latitude,longitude] WITHIN [[50.0,8.0],[51.0,9.0]]");
    List<ODocument> result = db.command(query).execute();
    Assert.assertEquals(INSERTCOUNT, result.size());
    db.close();

    db = dbPool.acquire();
    db.drop();

    File dbFolder = new File("./playground/db/db.2.2.13");
    Assert.assertEquals(false, dbFolder.exists());

    File dbFolderRoot = new File("./playground/db");
    dbFolderRoot.delete();
    Assert.assertEquals(false, dbFolderRoot.exists());
  }

  private void fillDb(ODatabaseDocumentTx db, int count) {
    for (int i = 0; i < count; i++) {
      ODocument doc = new ODocument("test");
      doc.field("name", "Test" + i);
      doc.field("latitude", 50.0 + (i * 0.000001));
      doc.field("longitude", 8.0 + (i * 0.000001));
      db.save(doc);
    }
    OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<ODocument>("select * from test");
    List<ODocument> result = db.command(query).execute();
    Assert.assertEquals(count, result.size());
  }
}
