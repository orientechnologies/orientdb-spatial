/*
 *
 *  * Copyright 2014 Orient Technologies.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  
 */

package com.orientechnologies.spatial.functions;

import com.orientechnologies.orient.core.command.OCommandContext;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.sql.functions.OSQLFunctionAbstract;
import com.orientechnologies.spatial.shape.OShapeFactory;
import com.spatial4j.core.shape.Shape;

import java.util.Map;

/**
 * Created by Enrico Risa on 06/08/15.
 */
public class OSTBufferFunction extends OSQLFunctionAbstract {

  public static final String NAME    = "ST_Buffer";

  OShapeFactory factory = OShapeFactory.INSTANCE;

  public OSTBufferFunction() {
    super(NAME, 2, 3);
  }

  @Override
  public Object execute(Object iThis, OIdentifiable iCurrentRecord, Object iCurrentResult, Object[] iParams,
      OCommandContext iContext) {
    Shape shape = factory.fromObject(iParams[0]);
    Number distance = (Number) iParams[1];
    Map params = null;
    if (iParams.length > 2) {
      params = (Map) iParams[2];
    }
    Shape buffer = factory.buffer(shape, distance.doubleValue(), params);
    return factory.toDoc(buffer);
  }

  @Override
  public String getSyntax() {
    return "ST_AsBuffer(<doc>)";
  }
}
