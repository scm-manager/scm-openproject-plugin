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

import com.cloudogu.openproject.dto.CommentPayload;
import com.cloudogu.openproject.dto.EmbeddedIssueStatus;
import com.cloudogu.openproject.dto.IssueStatus;
import com.cloudogu.openproject.dto.IssueUpdate;
import com.cloudogu.openproject.dto.OpenprojectIssue;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequest;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.net.ahc.AdvancedHttpResponse;
import sonia.scm.util.HttpUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenprojectRestApiServiceTest {

  private static final String BASE_URL = "localhost:3000";
  private static final String API_URL = "localhost:3000/api/v3";
  private static final String API_TOKEN = "secret";

  @Mock
  private AdvancedHttpRequest advancedHttpRequest;

  @Mock
  private AdvancedHttpRequestWithBody advancedHttpRequestWithBody;

  @Mock
  private AdvancedHttpResponse advancedHttpResponse;

  @Mock(answer = Answers.RETURNS_SELF)
  private AdvancedHttpClient advancedHttpClient;

  private OpenprojectRestApiService apiService;

  @BeforeEach
  void before() throws IOException {
    apiService = new OpenprojectRestApiService(advancedHttpClient, BASE_URL, API_TOKEN);
    lenient().when(advancedHttpRequest.request()).thenReturn(advancedHttpResponse);
    lenient().when(advancedHttpRequestWithBody.request()).thenReturn(advancedHttpResponse);
    lenient().when(advancedHttpRequest.spanKind("OpenProject")).thenReturn(advancedHttpRequest);
    lenient().when(advancedHttpRequestWithBody.spanKind("OpenProject")).thenReturn(advancedHttpRequestWithBody);
  }

  @Test
  void shouldRetrieveIssueById() throws IOException {
    when(advancedHttpClient.get(HttpUtil.concatenate(API_URL, OpenprojectRestApiService.ISSUES_PATH, "42"))).thenReturn(advancedHttpRequest);
    when(advancedHttpResponse.content()).thenReturn(Files.readString(Path.of(Resources.getResource("com/cloudogu/openproject/work_package_response.json").getFile())).getBytes());
    when(advancedHttpResponse.isSuccessful()).thenReturn(true);
    final OpenprojectIssue issue = apiService.getIssueById("#42");
    final IssueStatus issueStatus = issue.getStatus();
    assertThat(issueStatus.getName())
      .isEqualTo("done");
    assertThat(issueStatus.getId())
      .isEqualTo(7);
    assertThat(issueStatus.getLinks().getLinkBy("self").get().getHref())
      .isEqualTo("/api/v3/statuses/7");
  }

  @Test
  void shouldRetrieveIssueStatuses() throws IOException {
    when(advancedHttpClient.get(HttpUtil.concatenate(API_URL, OpenprojectRestApiService.ISSUE_STATUSES_PATH))).thenReturn(advancedHttpRequest);
    when(advancedHttpResponse.content()).thenReturn(Files.readString(Path.of(Resources.getResource("com/cloudogu/openproject/statuses_response.json").getFile())).getBytes());
    when(advancedHttpResponse.isSuccessful()).thenReturn(true);
    final List<IssueStatus> issueStatuses = apiService.getStatuses();
    assertThat(issueStatuses).hasSize(2);
    final IssueStatus issueStatus1 = issueStatuses.get(0);
    assertThat(issueStatus1.getName()).isEqualTo("new");
    assertThat(issueStatus1.getId()).isEqualTo(1);
    final IssueStatus issueStatus2 = issueStatuses.get(1);
    assertThat(issueStatus2.getName()).isEqualTo("done");
    assertThat(issueStatus2.getId()).isEqualTo(2);
  }

  @Test
  void shouldUpdateStateForIssueById() throws IOException {
    when(advancedHttpClient.post(HttpUtil.concatenate(API_URL, OpenprojectRestApiService.ISSUES_PATH, "1"))).thenReturn(advancedHttpRequestWithBody);
    when(advancedHttpResponse.isSuccessful()).thenReturn(true);

    OpenprojectIssue issue = createInProgressIssue();
    IssueStatus newStatus = createStatus(42, "done");

    apiService.updateStatus(issue, newStatus);

    verify(advancedHttpRequestWithBody).header("X-HTTP-Method-Override", "PATCH");
    verify(advancedHttpRequestWithBody).jsonContent(argThat(
      o -> {
        assertThat(o).isInstanceOf(IssueUpdate.class);
        IssueUpdate issueUpdate = (IssueUpdate) o;
        return issueUpdate.getId().equals(issue.getId())
          && issueUpdate.getLockVersion() == issue.getLockVersion()
          && issueUpdate.getLinks().getLinkBy("status").isPresent()
          && issueUpdate.getLinks().getLinkBy("status").get().getHref().equals(newStatus.getLinks().getLinkBy("self").get().getHref());
      }));
  }

  @Test
  void shouldAddCommentForIssueById() throws IOException {
    when(advancedHttpClient.post(HttpUtil.concatenate(API_URL, OpenprojectRestApiService.ISSUES_PATH, "1", "activities"))).thenReturn(advancedHttpRequestWithBody);
    when(advancedHttpResponse.isSuccessful()).thenReturn(true);

    OpenprojectIssue issue = createInProgressIssue();

    apiService.commentIssue(issue, "The first ten million years were the worst");

    verify(advancedHttpRequestWithBody).jsonContent(argThat(
      o -> {
        assertThat(o).isInstanceOf(CommentPayload.class);
        CommentPayload comment = (CommentPayload) o;
        return comment.getComment().getRaw().equals("The first ten million years were the worst");
      }));
  }

  @Test
  void shouldThrowOpenprojectExceptionOnGetIssueFailure() throws IOException {
    when(advancedHttpClient.get(anyString())).thenReturn(advancedHttpRequest);
    when(advancedHttpResponse.isSuccessful()).thenReturn(false);

    assertThatThrownBy(() -> apiService.getIssueById("#1"))
      .isInstanceOf(OpenprojectException.class);
  }

  @Test
  void shouldThrowOpenprojectExceptionOnUpdateIssueFailure() throws IOException {
    when(advancedHttpClient.post(anyString())).thenReturn(advancedHttpRequestWithBody);
    when(advancedHttpResponse.isSuccessful()).thenReturn(false);

    assertThatThrownBy(() -> apiService.updateStatus(createInProgressIssue(), createStatus(42, "done")))
      .isInstanceOf(OpenprojectException.class);
  }

  @Test
  void shouldThrowOpenprojectExceptionOnGetIssueStatusesFailure() throws IOException {
    when(advancedHttpClient.get(anyString())).thenReturn(advancedHttpRequest);
    when(advancedHttpResponse.isSuccessful()).thenReturn(false);

    assertThatThrownBy(() -> apiService.getStatuses())
      .isInstanceOf(OpenprojectException.class);
  }

  private static OpenprojectIssue createInProgressIssue() {
    return new OpenprojectIssue(1, 1, new EmbeddedIssueStatus(createStatus(23, "in progress")));
  }

  private static IssueStatus createStatus(int statusId, String status) {
    return new IssueStatus("http://op/statuses/" + statusId, statusId, status);
  }

}
