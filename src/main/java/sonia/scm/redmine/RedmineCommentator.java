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

import sonia.scm.issuetracker.spi.Commentator;
import sonia.scm.redmine.dto.RedmineIssue;

import java.io.IOException;

public class RedmineCommentator implements Commentator {

  private final RedmineRestApiService apiService;

  RedmineCommentator(RedmineRestApiService apiService) {
    this.apiService = apiService;
  }

  @Override
  public void comment(String issueKey, String comment) throws IOException {
    RedmineIssue issue = apiService.getIssueById(issueKey);
    issue.setNote(comment);
    apiService.updateIssue(issue);
  }
}
