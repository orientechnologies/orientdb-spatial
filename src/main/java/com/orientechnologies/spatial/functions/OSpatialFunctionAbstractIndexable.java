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

import com.orientechnologies.lucene.collections.OLuceneResultSet;
import com.orientechnologies.orient.core.command.OCommandContext;
import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;
import com.orientechnologies.orient.core.db.ODatabaseRecordThreadLocal;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.metadata.OMetadata;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.functions.OIndexableSQLFunction;
import com.orientechnologies.orient.core.sql.parser.*;
import com.orientechnologies.spatial.index.OLuceneSpatialIndex;
import com.orientechnologies.spatial.shape.OShapeFactory;
import com.orientechnologies.spatial.strategy.SpatialQueryBuilderAbstract;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Enrico Risa on 31/08/15.
 */
public abstract class OSpatialFunctionAbstractIndexable extends OSpatialFunctionAbstract implements OIndexableSQLFunction {

  OShapeFactory factory = OShapeFactory.INSTANCE;

  public OSpatialFunctionAbstractIndexable(String iName, int iMinParams, int iMaxParams) {
    super(iName, iMinParams, iMaxParams);
  }

  protected OLuceneSpatialIndex searchForIndex(OFromClause target, OExpression[] args) {
    OMetadata dbMetadata = getDb().getMetadata();

    OFromItem item = target.getItem();
    OIdentifier identifier = item.getIdentifier();
    String fieldName = args[0].toString();

    String className = identifier.getStringValue();
    List<OLuceneSpatialIndex> indices = dbMetadata
        .getSchema()
        .getClass(className)
        .getIndexes()
        .stream()
        .filter(idx -> idx instanceof OLuceneSpatialIndex)
        .map(idx -> (OLuceneSpatialIndex) idx)
        .filter(idx -> intersect(idx.getDefinition().getFields(), Arrays.asList(fieldName)))
        .collect(Collectors.toList());

    if (indices.size() > 1) {
      throw new IllegalArgumentException("too many indices matching given field name: " + String.join(",", fieldName));
    }

    return indices.size() == 0 ? null : indices.get(0);
  }

  protected OLuceneSpatialIndex searchForIndexOLd(OFromClause target, OExpression[] args) {

    // TODO Check if target is a class otherwise exception

    OFromItem item = target.getItem();
    OIdentifier identifier = item.getIdentifier();
    String fieldName = args[0].toString();

    Set<OIndex<?>> indexes = getDb().getMetadata().getIndexManager()
        .getClassInvolvedIndexes(identifier.getStringValue(), fieldName);
    for (OIndex<?> index : indexes) {
      if (index.getInternal() instanceof OLuceneSpatialIndex) {
        return (OLuceneSpatialIndex) index;
      }
    }
    return null;

  }

  protected ODatabaseDocumentInternal getDb() {
    return ODatabaseRecordThreadLocal.instance().get();
  }

  protected OLuceneResultSet results(OFromClause target, OExpression[] args, OCommandContext ctx, Object rightValue) {
    OIndex oIndex = searchForIndex(target, args);

    if (oIndex == null) {
      return null;
    }

    Map<String, Object> queryParams = new HashMap<>();
    queryParams.put(SpatialQueryBuilderAbstract.GEO_FILTER, operator());
    Object shape;
    if (args[1].getValue() instanceof OJson) {
      OJson json = (OJson) args[1].getValue();
      ODocument doc = new ODocument().fromJSON(json.toString());
      shape = doc.toMap();
    } else {
      shape = args[1].execute((OIdentifiable) null, ctx);
    }
    queryParams.put(SpatialQueryBuilderAbstract.SHAPE, shape);

    onAfterParsing(queryParams, args, ctx, rightValue);

    Set<String> indexes = (Set<String>) ctx.getVariable("involvedIndexes");
    if (indexes == null) {
      indexes = new HashSet<>();
      ctx.setVariable("involvedIndexes", indexes);
    }
    indexes.add(oIndex.getName());
    return (OLuceneResultSet) oIndex.get(queryParams);
  }

  protected void onAfterParsing(Map<String, Object> params, OExpression[] args, OCommandContext ctx, Object rightValue) {
  }

  protected abstract String operator();

  @Override
  public boolean canExecuteInline(OFromClause target, OBinaryCompareOperator operator, Object rightValue, OCommandContext ctx,
      OExpression... args) {

    return allowsIndexedExecution(target, operator, rightValue, ctx, args);
  }

  @Override
  public boolean allowsIndexedExecution(OFromClause target, OBinaryCompareOperator operator, Object rightValue, OCommandContext ctx,
      OExpression... args) {

    OLuceneSpatialIndex index = searchForIndex(target, args);

    return index != null;
  }

  @Override
  public boolean shouldExecuteAfterSearch(OFromClause target, OBinaryCompareOperator operator, Object rightValue,
      OCommandContext ctx, OExpression... args) {
    return false;
  }

  @Override
  public long estimate(OFromClause target, OBinaryCompareOperator operator, Object rightValue, OCommandContext ctx,
      OExpression... args) {

    OLuceneSpatialIndex index = searchForIndex(target, args);

    return index == null ? -1 : index.getSize();
  }

  public <T> boolean intersect(List<T> list1, List<T> list2) {

    for (T t : list1) {
      if (list2.contains(t)) {
        return true;
      }
    }

    return false;
  }

}
