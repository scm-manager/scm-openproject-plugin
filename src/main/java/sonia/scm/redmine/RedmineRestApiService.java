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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequest;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.net.ahc.AdvancedHttpResponse;
import sonia.scm.net.ahc.BaseHttpRequest;
import sonia.scm.redmine.config.RedmineConfiguration;
import sonia.scm.redmine.dto.IssueStatus;
import sonia.scm.redmine.dto.IssueStatusesResponse;
import sonia.scm.redmine.dto.RedmineIssue;
import sonia.scm.util.HttpUtil;

import java.io.IOException;
import java.util.List;

public class RedmineRestApiService {

  private final ObjectMapper objectMapper = new ObjectMapper();

  public static final String ISSUES_PATH = "work-packages";
  public static final String ISSUE_STATUSES_PATH = "issue_statuses";
  public static final String ISSUE_WRAPPER_FIELD_NAME = "issue";

  private final String url;
  private final String apiToken;
  private final AdvancedHttpClient httpClient;

  public RedmineRestApiService(AdvancedHttpClient httpClient, RedmineConfiguration configuration) {
    this(httpClient, configuration.getUrl(), configuration.getPassword());
  }

  public RedmineRestApiService(AdvancedHttpClient httpClient, String url, String apiToken) {
    this.httpClient = httpClient;
    this.url = url;
    this.apiToken = apiToken;
  }

  public RedmineIssue getIssueById(String issueKey) throws IOException {
    // Construct Request
    final AdvancedHttpRequest getIssueRequest = createGetRequest(HttpUtil.concatenate(ISSUES_PATH, Ids.parse(issueKey)));
    setRequestAuth(getIssueRequest);
    // Send request and handle response
    final AdvancedHttpResponse getIssueResponse = getIssueRequest.request();
    if (!getIssueResponse.isSuccessful()) {
      throw new RedmineException("Failed to retrieve issue", getIssueResponse.getStatus());
    }
    // Map response object
    return unwrapIssue(objectMapper.readTree(getIssueResponse.content()));
  }

  public void updateIssue(RedmineIssue issue) throws IOException {
    // Construct Request
    final AdvancedHttpRequestWithBody putRequest = createPutRequest(
      HttpUtil.concatenate(ISSUES_PATH, issue.getId().toString()),
      wrapIssue(issue)
    );

    // Send request and handle response
    final AdvancedHttpResponse putIssueResponse = putRequest.request();
    if (!putIssueResponse.isSuccessful()) {
      throw new RedmineException("Failed to update issue", putIssueResponse.getStatus());
    }
  }

  public List<IssueStatus> getStatuses() throws IOException {
    // Construct Request
    final AdvancedHttpRequest getIssueStatusesRequest = createGetRequest(ISSUE_STATUSES_PATH);
    // Send request and handle response
    final AdvancedHttpResponse getIssueStatusesResponse = getIssueStatusesRequest.request();
    if (!getIssueStatusesResponse.isSuccessful()) {
      throw new RedmineException("Failed to retrieve statuses", getIssueStatusesResponse.getStatus());
    }
    // Map response object
    return objectMapper.readValue(getIssueStatusesResponse.content(), IssueStatusesResponse.class).getIssueStatuses();
  }

  private AdvancedHttpRequest createGetRequest(String relativePath) {
    final AdvancedHttpRequest request = this.httpClient.get(createRequestUrl(relativePath)).spanKind("Redmine");
    setRequestAuth(request);
    return request;
  }

  private AdvancedHttpRequestWithBody createPutRequest(String relativePath, JsonNode payload) {
    final AdvancedHttpRequestWithBody request = this.httpClient.put(createRequestUrl(relativePath)).spanKind("Redmine");
    request.jsonContent(payload);
    setRequestAuth(request);
    return request;
  }

  private String createRequestUrl(String relativePath) {
    return HttpUtil.append(url, relativePath + ".json");
  }

  private void setRequestAuth(BaseHttpRequest<?> advancedHttpRequest) {
    advancedHttpRequest.basicAuth("apikey", apiToken);
  }

  private RedmineIssue unwrapIssue(JsonNode obj) {
    return new RedmineIssue((ObjectNode) obj.get(RedmineRestApiService.ISSUE_WRAPPER_FIELD_NAME));
  }

  private ObjectNode wrapIssue(RedmineIssue value) {
    final ObjectNode wrapper = objectMapper.createObjectNode();
    wrapper.set(RedmineRestApiService.ISSUE_WRAPPER_FIELD_NAME, value.toJsonNode());
    return wrapper;
  }
}
