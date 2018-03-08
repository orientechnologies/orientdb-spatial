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
package com.orientechnologies.spatial.operator;

import com.orientechnologies.lucene.collections.OLuceneResultSet;
import com.orientechnologies.orient.core.command.OCommandContext;
import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.index.OIndexCursor;
import com.orientechnologies.orient.core.index.OIndexCursorCollectionValue;
import com.orientechnologies.orient.core.index.OIndexCursorSingleValue;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.serialization.serializer.record.binary.ODocumentSerializer;
import com.orientechnologies.orient.core.sql.filter.OSQLFilterCondition;
import com.orientechnologies.spatial.collections.OSpatialCompositeKey;
import com.orientechnologies.spatial.strategy.SpatialQueryBuilderAbstract;
import com.orientechnologies.spatial.strategy.SpatialQueryBuilderOverlap;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.locationtech.spatial4j.shape.Shape;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OLuceneOverlapOperator extends OLuceneSpatialOperator {

  public OLuceneOverlapOperator() {
    super("&&", 5, false);
  }

  @Override
  public Collection<OIdentifiable> filterRecords(ODatabase<?> iRecord, List<String> iTargetClasses, OSQLFilterCondition iCondition,
      Object iLeft, Object iRight) {
    return null;
  }

  @Override
  public OIndexCursor executeIndexQuery(OCommandContext iContext, OIndex<?> index, List<Object> keyParams, boolean ascSortOrder) {
    OIndexCursor cursor;
    Object key;
    key = keyParams.get(0);

    Map<String, Object> queryParams = new HashMap<String, Object>();
    queryParams.put(SpatialQueryBuilderAbstract.GEO_FILTER, SpatialQueryBuilderOverlap.NAME);
    queryParams.put(SpatialQueryBuilderAbstract.SHAPE, key);

    long start = System.currentTimeMillis();
    OLuceneResultSet indexResult = (OLuceneResultSet) index.get(queryParams);
    if (indexResult != null)
      indexResult.sendLookupTime(iContext, start);

    if (indexResult == null || indexResult instanceof OIdentifiable)
      return new OIndexCursorSingleValue((OIdentifiable) indexResult, new OSpatialCompositeKey(keyParams));

    return new OIndexCursorCollectionValue(((Collection<OIdentifiable>) indexResult), new OSpatialCompositeKey(keyParams));

  }

  @Override
  public Object evaluateRecord(OIdentifiable iRecord, ODocument iCurrentResult, OSQLFilterCondition iCondition, Object iLeft,
      Object iRight, OCommandContext iContext, final ODocumentSerializer serializer) {
    Shape shape = factory.fromDoc((ODocument) iLeft);

    // TODO { 'shape' : { 'type' : 'LineString' , 'coordinates' : [[1,2],[4,6]]} }
    // TODO is not translated in map but in array[ { 'type' : 'LineString' , 'coordinates' : [[1,2],[4,6]]} ]
    Object filter;
    if (iRight instanceof Collection) {
      filter = ((Collection) iRight).iterator().next();
    } else {
      filter = iRight;
    }
    Shape shape1 = factory.fromObject(filter);

    return SpatialOperation.BBoxIntersects.evaluate(shape, shape1.getBoundingBox());
  }
}
