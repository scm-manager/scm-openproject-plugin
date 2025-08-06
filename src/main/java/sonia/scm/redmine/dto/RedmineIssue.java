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

package sonia.scm.redmine.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class RedmineIssue {

  private final ObjectNode node;

  public RedmineIssue(ObjectNode node) {
    this.node = node;
  }

  public void setStatus(IssueStatus status) {
    node.put("status_id", status.getId());
  }

  public void setNote(String note) {
    node.put("notes", note);
  }

  public IssueStatus getStatus() {
    ObjectNode statusNode = (ObjectNode) node.get("status");
    return new IssueStatus(
      statusNode.get("id").intValue(),
      statusNode.get("name").textValue()
    );
  }

  public Integer getId() {
    return node.get("id").intValue();
  }

  public JsonNode toJsonNode() {
    return node;
  }
}
