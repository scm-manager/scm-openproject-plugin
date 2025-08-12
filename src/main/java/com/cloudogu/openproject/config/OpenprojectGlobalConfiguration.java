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

package com.cloudogu.openproject.config;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;

@XmlRootElement(name = "openprojectGlobalConfiguration")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
@NoArgsConstructor
public class OpenprojectGlobalConfiguration extends OpenprojectConfiguration {

  private boolean disableRepositoryConfiguration;

  public OpenprojectGlobalConfiguration(String url, boolean autoClose, boolean updateIssues,
                                        boolean disableRepositoryConfiguration, String apiToken) {
    this(url, autoClose, updateIssues, disableRepositoryConfiguration, apiToken, false);
  }

  public OpenprojectGlobalConfiguration(String url, boolean autoClose, boolean updateIssues,
                                        boolean disableRepositoryConfiguration, String apiToken, boolean disableStateChangeByCommit) {
    super(url, autoClose, updateIssues, apiToken, Collections.emptyMap(), disableStateChangeByCommit);
    this.disableRepositoryConfiguration = disableRepositoryConfiguration;
  }

}
