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

package com.cloudogu.openproject.dto;

import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;

@NoArgsConstructor
@Getter
public class IssueUpdate extends HalRepresentation {
  private Integer id;
  private int lockVersion;

  public IssueUpdate(OpenprojectIssue issue, IssueStatus status) {
    super(Links.linkingTo().single(Link.link("status", getStatusLink(status))).build());
    this.id = issue.getId();
    this.lockVersion = issue.getLockVersion();
  }

  private static String getStatusLink(IssueStatus status) {
    Optional<Link> selfLink = status.getLinks().getLinkBy("self");
    return selfLink
      .orElseThrow(() -> new IllegalStateException(String.format("Status link 'self' not found for status %s with id %s", status.getName(), status.getId())))
      .getHref();
  }
}
