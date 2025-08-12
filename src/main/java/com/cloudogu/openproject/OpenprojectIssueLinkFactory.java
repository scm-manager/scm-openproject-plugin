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

import sonia.scm.issuetracker.IssueLinkFactory;
import sonia.scm.util.HttpUtil;

public class OpenprojectIssueLinkFactory implements IssueLinkFactory {

  private static final String PATH_ISSUES = "work_packages";

  private final String openprojectUrl;

  OpenprojectIssueLinkFactory(String openprojectUrl) {
    this.openprojectUrl = openprojectUrl;
  }

  @Override
  public String createLink(String key) {
    return HttpUtil.concatenate(openprojectUrl, PATH_ISSUES, Ids.parse(key));
  }
}
