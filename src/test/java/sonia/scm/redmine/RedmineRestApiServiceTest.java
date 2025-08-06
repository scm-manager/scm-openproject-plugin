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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequest;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.net.ahc.AdvancedHttpResponse;
import sonia.scm.redmine.dto.IssueStatus;
import sonia.scm.redmine.dto.RedmineIssue;
import sonia.scm.util.HttpUtil;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RedmineRestApiServiceTest {

  private static final String URL = "localhost:3000";
  private static final String USERNAME = "admin";
  private static final String PASSWORD = "secret";

  @Mock
  private AdvancedHttpRequest advancedHttpRequest;

  @Mock
  private AdvancedHttpRequestWithBody advancedHttpRequestWithBody;

  @Mock
  private AdvancedHttpResponse advancedHttpResponse;

  @Mock(answer = Answers.RETURNS_SELF)
  private AdvancedHttpClient advancedHttpClient;

  private RedmineRestApiService apiService;

  @Before
  public void before() throws IOException {
    apiService = new RedmineRestApiService(advancedHttpClient, URL, USERNAME, PASSWORD);
    when(advancedHttpRequest.request()).thenReturn(advancedHttpResponse);
    when(advancedHttpRequestWithBody.request()).thenReturn(advancedHttpResponse);
    when(advancedHttpRequest.spanKind("Redmine")).thenReturn(advancedHttpRequest);
    when(advancedHttpRequestWithBody.spanKind("Redmine")).thenReturn(advancedHttpRequestWithBody);
  }

  @Test
  public void shouldRetrieveIssueById() throws IOException {
    when(advancedHttpClient.get(HttpUtil.concatenate(URL, RedmineRestApiService.ISSUES_PATH, "1.json"))).thenReturn(advancedHttpRequest);
    when(advancedHttpResponse.content()).thenReturn("{\"issue\":{\"id\":1,\"project\":{\"id\":1,\"name\":\"default project\"},\"tracker\":{\"id\":1,\"name\":\"default tracker\"},\"status\":{\"id\":2,\"name\":\"done\"},\"priority\":{\"id\":1,\"name\":\"normal\"},\"author\":{\"id\":1,\"name\":\"Redmine Admin\"},\"subject\":\"test issue\",\"description\":\"\",\"start_date\":\"2020-09-30\",\"due_date\":null,\"done_ratio\":0,\"is_private\":false,\"estimated_hours\":null,\"total_estimated_hours\":null,\"created_on\":\"2020-09-30T15:18:57Z\",\"updated_on\":\"2020-10-02T10:24:59Z\",\"closed_on\":\"2020-10-02T10:24:59Z\"}}".getBytes());
    when(advancedHttpResponse.isSuccessful()).thenReturn(true);
    final RedmineIssue issue = apiService.getIssueById("#1");
    final IssueStatus issueStatus = issue.getStatus();
    assertThat(issueStatus.getName()).isEqualTo("done");
    assertThat(issueStatus.getId()).isEqualTo(2);
  }

  @Test
  public void shouldRetrieveIssueStatuses() throws IOException {
    when(advancedHttpClient.get(HttpUtil.concatenate(URL, RedmineRestApiService.ISSUE_STATUSES_PATH + ".json"))).thenReturn(advancedHttpRequest);
    when(advancedHttpResponse.content()).thenReturn("{\"issue_statuses\":[{\"id\":1,\"name\":\"new\",\"is_closed\":false},{\"id\":2,\"name\":\"done\",\"is_closed\":true}]}".getBytes());
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
  public void shouldUpdateIssueById() throws IOException {
    when(advancedHttpClient.put(HttpUtil.concatenate(URL, RedmineRestApiService.ISSUES_PATH, "1.json"))).thenReturn(advancedHttpRequestWithBody);
    when(advancedHttpResponse.isSuccessful()).thenReturn(true);

    final ObjectMapper objectMapper = new ObjectMapper();
    final RedmineIssue redmineIssue = new RedmineIssue((ObjectNode) objectMapper.readTree("{\"id\":1,\"project\":{\"id\":1,\"name\":\"default project\"},\"tracker\":{\"id\":1,\"name\":\"default tracker\"},\"status\":{\"id\":2,\"name\":\"done\"},\"priority\":{\"id\":1,\"name\":\"normal\"},\"author\":{\"id\":1,\"name\":\"Redmine Admin\"},\"subject\":\"test issue\",\"description\":\"\",\"start_date\":\"2020-09-30\",\"due_date\":null,\"done_ratio\":0,\"is_private\":false,\"estimated_hours\":null,\"total_estimated_hours\":null,\"created_on\":\"2020-09-30T15:18:57Z\",\"updated_on\":\"2020-10-02T10:24:59Z\",\"closed_on\":\"2020-10-02T10:24:59Z\"}"));
    final ObjectNode wrappedIssueNode = objectMapper.createObjectNode();
    wrappedIssueNode.set(RedmineRestApiService.ISSUE_WRAPPER_FIELD_NAME, redmineIssue.toJsonNode());

    apiService.updateIssue(redmineIssue);

    verify(advancedHttpRequestWithBody).jsonContent(wrappedIssueNode);
  }

  @Test
  public void shouldOnlyUpdateChangedValues() throws IOException {
    final ObjectMapper objectMapper = new ObjectMapper();
    AtomicReference<String> content = new AtomicReference<>();

    when(advancedHttpClient.put(HttpUtil.concatenate(URL, RedmineRestApiService.ISSUES_PATH, "1.json"))).thenReturn(advancedHttpRequestWithBody);
    when(advancedHttpResponse.isSuccessful()).thenReturn(true);
    when(advancedHttpRequestWithBody.jsonContent(any())).thenAnswer((call) -> {
      content.set(objectMapper.writeValueAsString(call.getArgument(0)));
      return advancedHttpRequestWithBody;
    });

    final RedmineIssue redmineIssue = new RedmineIssue((ObjectNode) objectMapper.readTree("{\"id\":1,\"project\":{\"id\":1,\"name\":\"default project\"},\"tracker\":{\"id\":1,\"name\":\"default tracker\"},\"status\":{\"id\":1,\"name\":\"ready\"},\"priority\":{\"id\":1,\"name\":\"normal\"},\"author\":{\"id\":1,\"name\":\"Redmine Admin\"},\"subject\":\"test issue\",\"description\":\"\",\"start_date\":\"2020-09-30\",\"due_date\":null,\"done_ratio\":0,\"is_private\":false,\"estimated_hours\":null,\"total_estimated_hours\":null,\"created_on\":\"2020-09-30T15:18:57Z\",\"updated_on\":\"2020-10-02T10:24:59Z\"}"));
    redmineIssue.setNote("a new note");
    redmineIssue.setStatus(new IssueStatus(2, "done"));
    final ObjectNode wrappedIssueNode = objectMapper.createObjectNode();
    wrappedIssueNode.set(RedmineRestApiService.ISSUE_WRAPPER_FIELD_NAME, redmineIssue.toJsonNode());

    apiService.updateIssue(redmineIssue);

    assertThat(content.get()).isEqualTo("{\"issue\":{\"id\":1,\"project\":{\"id\":1,\"name\":\"default project\"},\"tracker\":{\"id\":1,\"name\":\"default tracker\"},\"status\":{\"id\":1,\"name\":\"ready\"},\"priority\":{\"id\":1,\"name\":\"normal\"},\"author\":{\"id\":1,\"name\":\"Redmine Admin\"},\"subject\":\"test issue\",\"description\":\"\",\"start_date\":\"2020-09-30\",\"due_date\":null,\"done_ratio\":0,\"is_private\":false,\"estimated_hours\":null,\"total_estimated_hours\":null,\"created_on\":\"2020-09-30T15:18:57Z\",\"updated_on\":\"2020-10-02T10:24:59Z\",\"notes\":\"a new note\",\"status_id\":2}}");
  }

  @Test(expected = RedmineException.class)
  public void shouldThrowRedmineExceptionOnGetIssueFailure() throws IOException {
    when(advancedHttpClient.get(anyString())).thenReturn(advancedHttpRequest);
    when(advancedHttpResponse.isSuccessful()).thenReturn(false);
    apiService.getIssueById("#1");
  }

  @Test(expected = RedmineException.class)
  public void shouldThrowRedmineExceptionOnUpdateIssueFailure() throws IOException {
    when(advancedHttpClient.put(anyString())).thenReturn(advancedHttpRequestWithBody);
    when(advancedHttpResponse.isSuccessful()).thenReturn(false);
    final ObjectNode objectNode = new ObjectMapper().createObjectNode();
    objectNode.put("id", 1);
    apiService.updateIssue(new RedmineIssue(objectNode));
  }

  @Test(expected = RedmineException.class)
  public void shouldThrowRedmineExceptionOnGetIssueStatusesFailure() throws IOException {
    when(advancedHttpClient.get(anyString())).thenReturn(advancedHttpRequest);
    when(advancedHttpResponse.isSuccessful()).thenReturn(false);
    apiService.getStatuses();
  }

}
