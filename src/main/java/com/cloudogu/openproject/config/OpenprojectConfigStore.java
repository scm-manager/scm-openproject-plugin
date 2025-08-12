/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.cloudogu.openproject.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import sonia.scm.repository.Repository;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

@Singleton
public class OpenprojectConfigStore {

  public static final String NAME = "openproject";

  private final ConfigurationStoreFactory storeFactory;

  @Inject
  public OpenprojectConfigStore(ConfigurationStoreFactory storeFactory) {
    this.storeFactory = storeFactory;
  }

  public void storeConfiguration(OpenprojectGlobalConfiguration configuration) {
    createGlobalStore().set(configuration);
  }

  public void storeConfiguration(OpenprojectConfiguration configuration, Repository repository) {
    storeConfiguration(configuration, repository.getId());
  }

  public void storeConfiguration(OpenprojectConfiguration configuration, String repositoryId) {
    createStore(repositoryId).set(configuration);
  }

  public OpenprojectGlobalConfiguration getConfiguration() {
    return createGlobalStore().getOptional().orElse(new OpenprojectGlobalConfiguration());
  }

  public OpenprojectConfiguration getConfiguration(Repository repository) {
    return createStore(repository.getId()).getOptional().orElse(new OpenprojectConfiguration());
  }

  private ConfigurationStore<OpenprojectConfiguration> createStore(String repositoryId) {
    return storeFactory.withType(OpenprojectConfiguration.class).withName(NAME).forRepository(repositoryId).build();
  }

  private ConfigurationStore<OpenprojectGlobalConfiguration> createGlobalStore() {
    return storeFactory.withType(OpenprojectGlobalConfiguration.class).withName(NAME).build();
  }

}
