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
import com.cloudogu.openproject.config.OpenprojectConfigStore;
import com.cloudogu.openproject.config.OpenprojectGlobalConfiguration;
import jakarta.inject.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.issuetracker.api.IssueTracker;
import sonia.scm.issuetracker.spi.IssueTrackerBuilder;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenprojectIssueTrackerProviderTest {

  @Mock
  private IssueTrackerBuilder builder;

  private OpenprojectConfigStore configStore;

  @Mock
  private Provider<AdvancedHttpClient> httpClientProvider;

  @Mock
  private IssueTracker issueTracker;

  private OpenprojectIssueTrackerProvider issueTrackerProvider;

  private final Repository repository = RepositoryTestData.createHeartOfGold();

  @BeforeEach
  void setUpConfiguration() {
    configStore = new OpenprojectConfigStore(new InMemoryConfigurationStoreFactory());
    issueTrackerProvider = new OpenprojectIssueTrackerProvider(
      new ConfigurationResolver(configStore),
      httpClientProvider
    );
  }

  @Test
  void shouldNotCreateIssueTrackerWithoutValidConfiguration() {
    Optional<IssueTracker> tracker = issueTrackerProvider.create(builder, repository);
    assertThat(tracker).isEmpty();
  }

  @Test
  void shouldProvideReadOnlyIssueTracker() {
    OpenprojectGlobalConfiguration configuration = new OpenprojectGlobalConfiguration();
    configuration.setUrl("https://issues.hitchhiker.com");
    configStore.storeConfiguration(configuration);

    IssueTrackerBuilder.ReadStage readStage = mock(IssueTrackerBuilder.ReadStage.class);
    when(builder.start(any(), any(), any())).thenReturn(readStage);
    when(readStage.build()).thenReturn(issueTracker);

    Optional<IssueTracker> tracker = issueTrackerProvider.create(builder, repository);
    assertThat(tracker).isPresent();

    verify(readStage).build();
  }

  @Test
  void shouldProvideCommentingIssueTracker() {
    OpenprojectGlobalConfiguration configuration = new OpenprojectGlobalConfiguration();
    configuration.setUrl("https://issues.hitchhiker.com");
    configuration.setUpdateIssues(true);
    configStore.storeConfiguration(configuration);

    IssueTrackerBuilder.ReadStage readStage = mock(IssueTrackerBuilder.ReadStage.class);
    when(builder.start(any(), any(), any())).thenReturn(readStage);
    IssueTrackerBuilder.CommentingStage commentingStage = mock(IssueTrackerBuilder.CommentingStage.class);
    when(readStage.commenting(any(), any())).thenReturn(commentingStage);
    IssueTrackerBuilder.ChangeStateStage changeStateStage = mock(IssueTrackerBuilder.ChangeStateStage.class);
    when(commentingStage.template(any())).thenReturn(changeStateStage);
    when(changeStateStage.build()).thenReturn(issueTracker);

    Optional<IssueTracker> tracker = issueTrackerProvider.create(builder, repository);
    assertThat(tracker).isPresent();

    verify(changeStateStage).build();
  }

  @Test
  void shouldProvideStateChangingIssueTracker() {
    OpenprojectGlobalConfiguration configuration = new OpenprojectGlobalConfiguration();
    configuration.setUrl("https://issues.hitchhiker.com");
    configuration.setUpdateIssues(true);
    configuration.setAutoClose(true);
    configStore.storeConfiguration(configuration);

    IssueTrackerBuilder.ReadStage readStage = mock(IssueTrackerBuilder.ReadStage.class);
    when(builder.start(any(), any(), any())).thenReturn(readStage);
    IssueTrackerBuilder.CommentingStage commentingStage = mock(IssueTrackerBuilder.CommentingStage.class);
    when(readStage.commenting(any(), any())).thenReturn(commentingStage);
    IssueTrackerBuilder.ChangeStateStage changeStateStage = mock(IssueTrackerBuilder.ChangeStateStage.class);
    when(commentingStage.template(any())).thenReturn(changeStateStage);
    IssueTrackerBuilder.ChangeStateRenderStage changeStateRenderStage = mock(IssueTrackerBuilder.ChangeStateRenderStage.class);
    when(changeStateStage.stateChanging(any())).thenReturn(changeStateRenderStage);
    IssueTrackerBuilder.FinalStage finalStage = mock(IssueTrackerBuilder.FinalStage.class);
    when(changeStateRenderStage.template(any())).thenReturn(finalStage);
    when(finalStage.build()).thenReturn(issueTracker);

    Optional<IssueTracker> tracker = issueTrackerProvider.create(builder, repository);
    assertThat(tracker).isPresent();

    verify(finalStage).build();
  }

}
