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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.redmine.config.RedmineConfiguration;
import sonia.scm.redmine.dto.IssueStatus;
import sonia.scm.redmine.dto.RedmineIssue;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedmineStateChangerTest {

  private RedmineConfiguration configuration;

  @Mock
  private RedmineRestApiService apiService;

  private RedmineStateChanger stateChanger;

  @Mock
  private RedmineIssue issue;

  @BeforeEach
  void setUpStateChanger() {
    configuration = new RedmineConfiguration();
    stateChanger = new RedmineStateChanger(configuration, apiService);
  }

  @Test
  void shouldReturnKeyWords() throws IOException {
    List<IssueStatus> statusList = ImmutableList.of(
      new IssueStatus(0, "Open"),
      new IssueStatus(1, "Closed")
    );
    when(apiService.getStatuses()).thenReturn(statusList);

    Iterable<String> keyWords = stateChanger.getKeyWords("#42");
    assertThat(keyWords).containsOnly("Open", "Closed");
  }

  @Test
  void shouldReturnKeyWordsFromMapping() throws IOException {
    List<IssueStatus> statusList = ImmutableList.of(
      new IssueStatus(0, "Open"),
      new IssueStatus(1, "Closed")
    );
    configuration.setKeywordMapping(ImmutableMap.of("Closed", "closes, closing"));
    when(apiService.getStatuses()).thenReturn(statusList);

    Iterable<String> keyWords = stateChanger.getKeyWords("#42");
    assertThat(keyWords).containsOnly("Open", "Closed", "closes", "closing");
  }

  @Test
  void shouldTriggerStateChange() throws IOException {
    IssueStatus status = new IssueStatus(1, "Closed");
    when(apiService.getStatuses()).thenReturn(Collections.singletonList(status));
    when(apiService.getIssueById("#21")).thenReturn(issue);

    stateChanger.changeState("#21", "Closed");

    verify(issue).setStatus(status);
    verify(apiService).updateIssue(issue);
  }

  @Test
  void shouldTriggerStateChangeWithMappedKeyWord() throws IOException {
    IssueStatus status = new IssueStatus(1, "Closed");
    configuration.setKeywordMapping(ImmutableMap.of("Closed", "fixes, closes"));

    when(apiService.getStatuses()).thenReturn(Collections.singletonList(status));
    when(apiService.getIssueById("#21")).thenReturn(issue);

    stateChanger.changeState("#21", "closes");

    verify(issue).setStatus(status);
    verify(apiService).updateIssue(issue);
  }

  @Test
  void shouldDisableUpdatesByCommits() {
    configuration.setDisableStateChangeByCommit(true);

    assertThat(stateChanger.isStateChangeActivatedForCommits()).isFalse();
  }

  @Test
  void shouldEnableUpdatesByCommitsByDefault() {
    assertThat(stateChanger.isStateChangeActivatedForCommits()).isTrue();
  }
}
