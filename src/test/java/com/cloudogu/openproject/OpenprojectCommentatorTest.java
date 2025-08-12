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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenprojectCommentatorTest {

  @Mock
  private OpenprojectRestApiService apiService;

  @InjectMocks
  private OpenprojectCommentator commentator;

  @Mock
  private OpenprojectIssue issue;

  @Test
  void shouldComment() throws IOException {
    when(apiService.getIssueById("#42")).thenReturn(issue);

    commentator.comment("#42", "Awesome note");

    verify(apiService).commentIssue(issue, "Awesome note");
  }

}
