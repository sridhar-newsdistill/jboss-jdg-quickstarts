/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.quickstarts.datagrid.eap.app;

import java.util.logging.Logger;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;

import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.transaction.TransactionMode;
import org.infinispan.transaction.lookup.GenericTransactionManagerLookup;

/**
 * Creates a DefaultCacheManager which is configured with a configuration file.
 * <b>Infinispan's libraries need to be provided as module with dependency, also bundling with the application is possible.</b>
 * 
 * @author Wolf-Dieter Fink
 */
@ApplicationScoped
public class App2CacheManagerProvider {
   private static final Logger log = Logger.getLogger(App2CacheManagerProvider.class.getName());
   private DefaultCacheManager manager;

   public DefaultCacheManager getCacheManager() {
      if (manager == null) {
         log.info("construct a App2CacheManager");

         GlobalConfiguration glob = new GlobalConfigurationBuilder().clusteredDefault() // Builds a default clustered
               // notice the file MUST NOT named jgroups-udp.xml as this is provided by the infinispan module and prefered !
               .transport().addProperty("configurationFile", "jgroups-admin2.xml") // provide a specific JGroups configuration
               .clusterName("ClusterTwo").globalJmxStatistics().allowDuplicateDomains(true).enable() // This method enables the jmx statistics of
               // the global configuration and allows for duplicate JMX domains
               .build(); // Builds the GlobalConfiguration object
         Configuration loc = new ConfigurationBuilder().jmxStatistics().enable() // Enable JMX statistics
               .clustering().cacheMode(CacheMode.DIST_SYNC) // Set Cache mode to SYNC DISTRIBUTED
               .build();
         manager = new DefaultCacheManager(glob, loc, true);

         Configuration cache = new ConfigurationBuilder().clustering().cacheMode(CacheMode.DIST_SYNC).transaction()
               .transactionMode(TransactionMode.TRANSACTIONAL)
               .transactionManagerLookup(new GenericTransactionManagerLookup()).build();
         manager.defineConfiguration("App2Cache", cache);
      }
      return manager;
   }

   @PreDestroy
   public void cleanUp() {
      manager.stop();
      manager = null;
   }

}
