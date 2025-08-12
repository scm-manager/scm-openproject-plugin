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

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Repository;

import java.util.Optional;

public final class ConfigurationResolver {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigurationResolver.class);

  private final OpenprojectConfigStore configurationStore;

  @Inject
  public ConfigurationResolver(OpenprojectConfigStore configurationStore) {
    this.configurationStore = configurationStore;
  }

  public Optional<OpenprojectConfiguration> resolve(Repository repository) {
    OpenprojectGlobalConfiguration globalConfiguration = configurationStore.getConfiguration();

    if (globalConfiguration.isDisableRepositoryConfiguration()) {
      if (!globalConfiguration.isValid()) {
        LOG.debug("global openproject config is not valid, but disables repository config; no config returned");
        return Optional.empty();
      }
      return Optional.of(globalConfiguration);
    }

    OpenprojectConfiguration configuration = configurationStore.getConfiguration(repository);

    if (!configuration.isValid()) {
      LOG.debug("repository config for {}/{} is not valid, falling back to global config", repository.getNamespace(), repository.getName());
      configuration = globalConfiguration;
    }

    if (!configuration.isValid()) {
      LOG.debug("no valid configuration for repository {}/{} found", repository.getNamespace(), repository.getName());
      return Optional.empty();
    }
    return Optional.of(configuration);
  }

}
