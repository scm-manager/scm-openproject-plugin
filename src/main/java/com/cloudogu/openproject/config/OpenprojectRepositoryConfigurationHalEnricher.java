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

import com.cloudogu.openproject.Constants;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import sonia.scm.api.v2.resources.Enrich;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricher;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;

@Extension
@Enrich(Repository.class)
public class OpenprojectRepositoryConfigurationHalEnricher implements HalEnricher {

  private final Provider<ScmPathInfoStore> scmPathInfoStoreProvider;
  private final OpenprojectConfigStore openprojectConfigurationStore;

  @Inject
  public OpenprojectRepositoryConfigurationHalEnricher(Provider<ScmPathInfoStore> scmPathInfoStoreProvider,
                                                   OpenprojectConfigStore openprojectConfigurationStore) {
    this.scmPathInfoStoreProvider = scmPathInfoStoreProvider;
    this.openprojectConfigurationStore = openprojectConfigurationStore;
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    Repository repository = context.oneRequireByType(Repository.class);
    if (!openprojectConfigurationStore.getConfiguration().isDisableRepositoryConfiguration() && RepositoryPermissions.custom(Constants.NAME, repository).isPermitted()) {
      String linkBuilder = new LinkBuilder(scmPathInfoStoreProvider.get().get(), OpenprojectConfigurationResource.class)
        .method("getConfiguration")
        .parameters(repository.getNamespace(), repository.getName())
        .href();

      appender.appendLink("openprojectConfig", linkBuilder);
    }
  }



}

