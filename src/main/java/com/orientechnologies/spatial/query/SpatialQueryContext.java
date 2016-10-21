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
package com.orientechnologies.spatial.query;

import com.orientechnologies.lucene.query.QueryContext;
import com.orientechnologies.orient.core.command.OCommandContext;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.spatial.query.SpatialArgs;

/**
 * Created by Enrico Risa on 08/01/15.
 */
public class SpatialQueryContext extends QueryContext {

  public SpatialArgs spatialArgs;

  public SpatialQueryContext(OCommandContext context, IndexSearcher searcher, Query query) {
    super(context, searcher, query);
  }

  public SpatialQueryContext(OCommandContext context, IndexSearcher searcher, Query query, Sort sort) {
    super(context, searcher, query, sort);
  }

  public SpatialQueryContext setSpatialArgs(SpatialArgs spatialArgs) {
    this.spatialArgs = spatialArgs;
    return this;
  }
}
