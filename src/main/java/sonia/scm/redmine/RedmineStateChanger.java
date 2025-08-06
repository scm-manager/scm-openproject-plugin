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

import com.google.common.base.Splitter;
import com.google.common.collect.Streams;
import sonia.scm.issuetracker.spi.StateChanger;
import sonia.scm.redmine.config.RedmineConfiguration;
import sonia.scm.redmine.dto.IssueStatus;
import sonia.scm.redmine.dto.RedmineIssue;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RedmineStateChanger implements StateChanger {

  private final RedmineConfiguration configuration;
  private final RedmineRestApiService apiService;
  private List<IssueStatus> statusList;

  RedmineStateChanger(RedmineConfiguration configuration, RedmineRestApiService apiService) {
    this.configuration = configuration;
    this.apiService = apiService;
  }

  @Override
  public void changeState(String issueKey, String keyWord) throws IOException {
    RedmineIssue issue = apiService.getIssueById(issueKey);
    IssueStatus status = getIssueStatusByKeyword(resolveMapping(keyWord));
    issue.setStatus(status);
    apiService.updateIssue(issue);
  }

  @Override
  public Iterable<String> getKeyWords(String issueKey) throws IOException {
    return Streams.concat(getStatusKeyWords(), getMappingKeyWords())
      .collect(Collectors.toList());
  }

  @Override
  public boolean isStateChangeActivatedForCommits() {
    return !configuration.isDisableStateChangeByCommit();
  }

  private String resolveMapping(String keyWord) {
    for (Map.Entry<String, String> entry : configuration.getKeywordMapping().entrySet()) {
      List<String> values = split().splitToList(entry.getValue());
      if (values.contains(keyWord)) {
        return entry.getKey();
      }
    }
    return keyWord;
  }

  @SuppressWarnings("UnstableApiUsage")
  private Stream<String> getMappingKeyWords() {
    return configuration.getKeywordMapping().values()
      .stream()
      .flatMap(v -> split().splitToStream(v));
  }

  private Splitter split() {
    return Splitter.on(',').omitEmptyStrings().trimResults();
  }

  private Stream<String> getStatusKeyWords() throws IOException {
    return getStatusList()
      .stream()
      .map(IssueStatus::getName);
  }

  private IssueStatus getIssueStatusByKeyword(String keyword) throws IOException {
    return getStatusList()
      .stream()
      .filter(status -> keyword.equalsIgnoreCase(status.getName()))
      .findAny()
      .orElseThrow(() -> new IOException("Could not find status for key word " + keyword));
  }

  private List<IssueStatus> getStatusList() throws IOException {
    if (statusList == null) {
      statusList = apiService.getStatuses();
    }
    return statusList;
  }
}
