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

import com.cloudogu.openproject.dto.OpenprojectIssue;
import sonia.scm.issuetracker.spi.Commentator;

import java.io.IOException;

public class OpenprojectCommentator implements Commentator {

  private final OpenprojectRestApiService apiService;

  OpenprojectCommentator(OpenprojectRestApiService apiService) {
    this.apiService = apiService;
  }

  @Override
  public void comment(String issueKey, String comment) throws IOException {
    OpenprojectIssue issue = apiService.getIssueById(issueKey);
    apiService.commentIssue(issue, comment);
  }
}
