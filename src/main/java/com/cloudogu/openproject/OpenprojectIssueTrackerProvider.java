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

package com.cloudogu.openproject;

import com.cloudogu.openproject.config.ConfigurationResolver;
import com.cloudogu.openproject.config.OpenprojectConfiguration;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import sonia.scm.issuetracker.api.IssueTracker;
import sonia.scm.issuetracker.spi.IssueTrackerBuilder;
import sonia.scm.issuetracker.spi.IssueTrackerProvider;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Repository;

import java.util.Optional;

@Extension
public class OpenprojectIssueTrackerProvider implements IssueTrackerProvider {

  private final ConfigurationResolver configurationResolver;
  private final Provider<AdvancedHttpClient> httpClientProvider;

  @Inject
  public OpenprojectIssueTrackerProvider(ConfigurationResolver configurationResolver, Provider<AdvancedHttpClient> httpClientProvider) {
    this.configurationResolver = configurationResolver;
    this.httpClientProvider = httpClientProvider;
  }

  @Override
  public Optional<IssueTracker> create(IssueTrackerBuilder builder, Repository repository) {
    return configurationResolver.resolve(repository)
      .map(configuration -> create(builder, configuration, repository));
  }

  private IssueTracker create(IssueTrackerBuilder builder, OpenprojectConfiguration configuration, Repository repository) {
    OpenprojectIssueMatcher matcher = new OpenprojectIssueMatcher();
    OpenprojectRestApiService apiService = new OpenprojectRestApiService(httpClientProvider.get(), configuration);
    OpenprojectIssueLinkFactory linkFactory = apiService.getLinkFactory();

    IssueTrackerBuilder.ReadStage readStage = builder.start(Constants.NAME, matcher, linkFactory);
    if (configuration.isUpdateIssuesEnabled()) {
      IssueTrackerBuilder.ChangeStateStage changeStateStage = readStage.commenting(repository, new OpenprojectCommentator(apiService))
        .template(referenceTemplate());

      if (configuration.isAutoCloseEnabled()) {
        return changeStateStage.stateChanging(new OpenprojectStateChanger(configuration, apiService))
          .template(stateChangeTemplate())
          .build();
      }

      return changeStateStage.build();
    }
    return readStage.build();
  }

  private String stateChangeTemplate() {
    return template("statechange");
  }

  private String referenceTemplate() {
    return template("reference");
  }

  private String template(String type) {
    return String.format("/scm/template/markdown/{0}_%s.mustache", type);
  }

}
