/*
 * Copyright 2014 Orient Technologies.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.orientechnologies.spatial;

import com.orientechnologies.common.log.OLogManager;
import com.orientechnologies.lucene.functions.OLuceneFunctionsFactory;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.db.ODatabaseInternal;
import com.orientechnologies.orient.core.db.ODatabaseLifecycleListener;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.index.OIndexes;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OSQLEngine;
import com.orientechnologies.orient.core.sql.functions.OSQLFunction;
import com.orientechnologies.orient.core.sql.operator.OQueryOperator;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.config.OServerParameterConfiguration;
import com.orientechnologies.orient.server.plugin.OServerPluginAbstract;
import com.orientechnologies.spatial.functions.OSpatialFunctionsFactory;
import com.orientechnologies.spatial.index.OLuceneSpatialIndex;
import com.orientechnologies.spatial.operator.OLuceneSpatialOperatorFactory;
import com.orientechnologies.spatial.shape.OShapeFactory;
import org.apache.lucene.util.Version;

public class OLuceneSpatialIndexPlugin extends OServerPluginAbstract implements ODatabaseLifecycleListener {

  private OLuceneSpatialManager spatialManager;

  public OLuceneSpatialIndexPlugin() {
  }

  @Override
  public String getName() {
    return "spatial-index";
  }

  @Override
  public void startup() {
    super.startup();
    OLogManager.instance().info(this, "Spatial index plugin startup");

    Orient.instance().addDbLifecycleListener(this);

    OIndexes.registerFactory(new OLuceneSpatialIndexFactory(true));

    registerOperators();

    registerFunctions();

    spatialManager = new OLuceneSpatialManager(OShapeFactory.INSTANCE);
    OLogManager.instance().info(this, "Spatial index plugin installed and active. Lucene version: %s", Version.LATEST);
  }

  protected void registerOperators() {

    for (OQueryOperator operator : OLuceneSpatialOperatorFactory.OPERATORS) {
      OSQLEngine.registerOperator(operator);
    }

  }

  protected void registerFunctions() {

    for (String s : OSpatialFunctionsFactory.FUNCTIONS.keySet()) {
      OSQLEngine.getInstance().registerFunction(s, (OSQLFunction) OLuceneFunctionsFactory.FUNCTIONS.get(s));
    }

  }

  @Override
  public void config(OServer oServer, OServerParameterConfiguration[] iParams) {

  }

  @Override
  public void shutdown() {
    super.shutdown();
  }

  @Override
  public PRIORITY getPriority() {
    return PRIORITY.REGULAR;
  }

  @Override
  public void onCreate(ODatabaseInternal iDatabase) {
    spatialManager.init((ODatabaseDocumentTx) iDatabase);
  }

  @Override
  public void onOpen(ODatabaseInternal iDatabase) {
    spatialManager.init((ODatabaseDocumentTx) iDatabase);
  }

  @Override
  public void onClose(final ODatabaseInternal iDatabase) {
  }

  @Override
  public void onDrop(final ODatabaseInternal iDatabase) {
    OLogManager.instance().info(this, "Dropping spatial indexes...");
    for (OIndex idx : iDatabase.getMetadata().getIndexManager().getIndexes()) {
      if (idx.getInternal() instanceof OLuceneSpatialIndex) {
        OLogManager.instance().info(this, "- index '%s'", idx.getName());
        idx.delete();
      }
    }
  }

  @Override
  public void onCreateClass(final ODatabaseInternal iDatabase, final OClass iClass) {
  }

  @Override
  public void onDropClass(final ODatabaseInternal iDatabase, final OClass iClass) {
  }

  @Override
  public void onLocalNodeConfigurationRequest(ODocument iConfiguration) {
  }
}
