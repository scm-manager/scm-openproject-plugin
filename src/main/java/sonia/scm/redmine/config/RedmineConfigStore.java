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

package sonia.scm.redmine.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import sonia.scm.repository.Repository;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;

@Singleton
public class RedmineConfigStore {

  public static final String NAME = "redmine";

  private final ConfigurationStoreFactory storeFactory;

  @Inject
  public RedmineConfigStore(ConfigurationStoreFactory storeFactory) {
    this.storeFactory = storeFactory;
  }

  public void storeConfiguration(RedmineGlobalConfiguration configuration) {
    createGlobalStore().set(configuration);
  }

  public void storeConfiguration(RedmineConfiguration configuration, Repository repository) {
    storeConfiguration(configuration, repository.getId());
  }

  public void storeConfiguration(RedmineConfiguration configuration, String repositoryId) {
    createStore(repositoryId).set(configuration);
  }

  public RedmineGlobalConfiguration getConfiguration() {
    return createGlobalStore().getOptional().orElse(new RedmineGlobalConfiguration());
  }

  public RedmineConfiguration getConfiguration(Repository repository) {
    return createStore(repository.getId()).getOptional().orElse(new RedmineConfiguration());
  }

  private ConfigurationStore<RedmineConfiguration> createStore(String repositoryId) {
    return storeFactory.withType(RedmineConfiguration.class).withName(NAME).forRepository(repositoryId).build();
  }

  private ConfigurationStore<RedmineGlobalConfiguration> createGlobalStore() {
    return storeFactory.withType(RedmineGlobalConfiguration.class).withName(NAME).build();
  }

}
