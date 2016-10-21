/**
 * Copyright 2010-2016 OrientDB LTD (http://orientdb.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For more information: http://www.orientdb.com
 */
package com.orientechnologies.spatial.strategy;

import com.orientechnologies.spatial.engine.OLuceneSpatialIndexContainer;
import com.orientechnologies.spatial.query.SpatialQueryContext;
import com.orientechnologies.spatial.shape.OShapeBuilder;
import org.locationtech.spatial4j.shape.Shape;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialOperation;

import java.util.Map;

/**
 * Created by Enrico Risa on 11/08/15.
 */
public class SpatialQueryBuilderDWithin extends SpatialQueryBuilderAbstract {

  public static final String NAME = "dwithin";

  public SpatialQueryBuilderDWithin(OLuceneSpatialIndexContainer manager, OShapeBuilder factory) {
    super(manager, factory);
  }

  @Override
  public SpatialQueryContext build(Map<String, Object> query) throws Exception {
    Shape shape = parseShape(query);

    SpatialStrategy strategy = manager.strategy();

    Number distance = (Number) query.get("distance");
    if (distance != null) {
      shape = shape.getBuffered(distance.doubleValue(), factory.context());
    }

    if (isOnlyBB(strategy)) {
      shape = shape.getBoundingBox();

    }
    SpatialArgs args1 = new SpatialArgs(SpatialOperation.Intersects, shape);

    Query filterQuery = strategy.makeQuery(args1);

    BooleanQuery q = new BooleanQuery.Builder().add(filterQuery, BooleanClause.Occur.MUST)
        .add(new MatchAllDocsQuery(), BooleanClause.Occur.SHOULD).build();

    return new SpatialQueryContext(null, manager.searcher(), q);
  }

  @Override
  public String getName() {
    return NAME;
  }
}
