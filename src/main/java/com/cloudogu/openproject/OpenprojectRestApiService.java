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

import com.cloudogu.openproject.config.OpenprojectConfiguration;
import com.cloudogu.openproject.dto.CommentPayload;
import com.cloudogu.openproject.dto.IssueStatus;
import com.cloudogu.openproject.dto.IssueStatusesResponse;
import com.cloudogu.openproject.dto.IssueUpdate;
import com.cloudogu.openproject.dto.OpenprojectIssue;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequest;
import sonia.scm.net.ahc.AdvancedHttpRequestWithBody;
import sonia.scm.net.ahc.AdvancedHttpResponse;
import sonia.scm.net.ahc.BaseHttpRequest;
import sonia.scm.util.HttpUtil;

import java.io.IOException;
import java.util.List;

@Slf4j
public class OpenprojectRestApiService {

  private static final String SPAN_KIND = "OpenProject";

  private final ObjectMapper objectMapper = new ObjectMapper();

  public static final String ISSUES_PATH = "work_packages";
  public static final String ISSUE_STATUSES_PATH = "statuses";

  private final AdvancedHttpClient httpClient;
  private final String baseUrl;
  private final String apiUrl;
  private final String apiToken;

  public OpenprojectRestApiService(AdvancedHttpClient httpClient, OpenprojectConfiguration configuration) {
    this(httpClient, configuration.getUrl(), configuration.getApiToken());
  }

  public OpenprojectRestApiService(AdvancedHttpClient httpClient, String baseUrl, String apiToken) {
    this.httpClient = httpClient;
    this.baseUrl = baseUrl;
    this.apiUrl = HttpUtil.concatenate(baseUrl, "api", "v3");
    this.apiToken = apiToken;
  }

  public OpenprojectIssue getIssueById(String issueKey) throws IOException {
    log.debug("Retrieving issue with id: {}", issueKey);
    AdvancedHttpRequest getIssueRequest = createGetRequest(HttpUtil.concatenate(ISSUES_PATH, Ids.parse(issueKey)));
    setRequestAuth(getIssueRequest);

    AdvancedHttpResponse getIssueResponse = getIssueRequest.request();
    if (!getIssueResponse.isSuccessful()) {
      throw new OpenprojectException("Failed to retrieve issue", getIssueResponse.getStatus());
    }
    log.trace("Issue retrieved successfully");
    return objectMapper.readValue(getIssueResponse.content(), OpenprojectIssue.class);
  }

  public void updateStatus(OpenprojectIssue issue, IssueStatus newStatus) throws IOException {
    log.debug("Updating issue {} to status {}", issue.getId(), newStatus.getName());
    IssueUpdate issueUpdate = new IssueUpdate(issue, newStatus);

    AdvancedHttpRequestWithBody request = createPatchRequest(
      HttpUtil.concatenate(ISSUES_PATH, issueUpdate.getId().toString()),
      issueUpdate
    );

    AdvancedHttpResponse putIssueResponse = request.request();
    if (!putIssueResponse.isSuccessful()) {
      throw new OpenprojectException("Failed to update issue", putIssueResponse.getStatus());
    }
    log.trace("Issue {} updated to status {}", issue.getId(), newStatus.getName());
  }

  public List<IssueStatus> getStatuses() throws IOException {
    log.debug("Retrieving issue statuses");
    AdvancedHttpRequest getIssueStatusesRequest = createGetRequest(ISSUE_STATUSES_PATH);

    AdvancedHttpResponse getIssueStatusesResponse = getIssueStatusesRequest.request();
    if (!getIssueStatusesResponse.isSuccessful()) {
      throw new OpenprojectException("Failed to retrieve statuses", getIssueStatusesResponse.getStatus());
    }

    log.trace("Issue statuses retrieved successfully");
    return objectMapper.readValue(getIssueStatusesResponse.content(), IssueStatusesResponse.class).getEmbedded().getIssueStatuses();
  }

  public void commentIssue(OpenprojectIssue issue, String comment) throws IOException {
    log.debug("Adding comment to issue {}", issue.getId());
    String path = HttpUtil.concatenate(ISSUES_PATH, issue.getId().toString(), "activities");
    AdvancedHttpRequestWithBody request = httpClient.post(createRequestUrl(path)).spanKind(SPAN_KIND);
    request.jsonContent(new CommentPayload(comment));
    setRequestAuth(request);
    AdvancedHttpResponse putIssueResponse = request.request();
    if (!putIssueResponse.isSuccessful()) {
      throw new OpenprojectException("Failed to update issue", putIssueResponse.getStatus());
    }
    log.trace("Comment added to issue {}", issue.getId());
  }

  private AdvancedHttpRequest createGetRequest(String relativePath) {
    AdvancedHttpRequest request = httpClient.get(createRequestUrl(relativePath)).spanKind(SPAN_KIND);
    setRequestAuth(request);
    return request;
  }

  private AdvancedHttpRequestWithBody createPatchRequest(String relativePath, Object payload) {
    AdvancedHttpRequestWithBody request = httpClient.post(createRequestUrl(relativePath)).spanKind(SPAN_KIND);
    request.jsonContent(payload);
    request.header("X-HTTP-Method-Override", "PATCH");
    setRequestAuth(request);
    return request;
  }

  private String createRequestUrl(String relativePath) {
    return HttpUtil.append(apiUrl, relativePath);
  }

  private void setRequestAuth(BaseHttpRequest<?> advancedHttpRequest) {
    advancedHttpRequest.basicAuth("apikey", apiToken);
  }

  public OpenprojectIssueLinkFactory getLinkFactory() {
    return new OpenprojectIssueLinkFactory(baseUrl);
  }
}
