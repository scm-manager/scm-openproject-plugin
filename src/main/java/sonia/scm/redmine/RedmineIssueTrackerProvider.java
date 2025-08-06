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

package sonia.scm.redmine;

import sonia.scm.issuetracker.api.IssueTracker;
import sonia.scm.issuetracker.spi.IssueTrackerBuilder;
import sonia.scm.issuetracker.spi.IssueTrackerProvider;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.plugin.Extension;
import sonia.scm.redmine.config.ConfigurationResolver;
import sonia.scm.redmine.config.RedmineConfiguration;
import sonia.scm.repository.Repository;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import java.util.Locale;
import java.util.Optional;

@Extension
public class RedmineIssueTrackerProvider implements IssueTrackerProvider {

  private final ConfigurationResolver configurationResolver;
  private final Provider<AdvancedHttpClient> httpClientProvider;

  @Inject
  public RedmineIssueTrackerProvider(ConfigurationResolver configurationResolver, Provider<AdvancedHttpClient> httpClientProvider) {
    this.configurationResolver = configurationResolver;
    this.httpClientProvider = httpClientProvider;
  }

  @Override
  public Optional<IssueTracker> create(IssueTrackerBuilder builder, Repository repository) {
    return configurationResolver.resolve(repository)
      .map(configuration -> create(builder, configuration, repository));
  }

  private IssueTracker create(IssueTrackerBuilder builder, RedmineConfiguration configuration, Repository repository) {
    RedmineIssueMatcher matcher = new RedmineIssueMatcher();
    RedmineIssueLinkFactory linkFactory = new RedmineIssueLinkFactory(configuration.getUrl());
    RedmineRestApiService apiService = new RedmineRestApiService(httpClientProvider.get(), configuration);

    IssueTrackerBuilder.ReadStage readStage = builder.start(Constants.NAME, matcher, linkFactory);
    if (configuration.isUpdateIssuesEnabled()) {
      IssueTrackerBuilder.ChangeStateStage changeStateStage = readStage.commenting(repository, new RedmineCommentator(apiService))
        .template(referenceTemplate(configuration));

      if (configuration.isAutoCloseEnabled()) {
        return changeStateStage.stateChanging(new RedmineStateChanger(configuration, apiService))
          .template(stateChangeTemplate(configuration))
          .build();
      }

      return changeStateStage.build();
    }
    return readStage.build();
  }

  private String stateChangeTemplate(RedmineConfiguration configuration) {
    return template(configuration, "statechange");
  }

  private String referenceTemplate(RedmineConfiguration configuration) {
    return template(configuration, "reference");
  }

  private String template(RedmineConfiguration configuration, String type) {
    String textFormat = configuration.getTextFormatting().name().toLowerCase(Locale.ENGLISH);
    return String.format("/scm/template/%s/{0}_%s.mustache", textFormat, type);
  }

}
