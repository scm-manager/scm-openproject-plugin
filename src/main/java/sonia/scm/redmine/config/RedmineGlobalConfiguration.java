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

package sonia.scm.redmine.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.Collections;

/**
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "redmineGlobalConfiguration")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
@NoArgsConstructor
public class RedmineGlobalConfiguration extends RedmineConfiguration {

  private boolean disableRepositoryConfiguration;

  public RedmineGlobalConfiguration(String url, TextFormatting textFormatting, boolean autoClose, boolean updateIssues,
                                    boolean disableRepositoryConfiguration, String username, String password) {
    this(url, textFormatting, autoClose, updateIssues, disableRepositoryConfiguration, username, password, false);
  }

  public RedmineGlobalConfiguration(String url, TextFormatting textFormatting, boolean autoClose, boolean updateIssues,
                                    boolean disableRepositoryConfiguration, String username, String password, boolean disableStateChangeByCommit) {
    super(url, textFormatting, autoClose, updateIssues, username, password, Collections.emptyMap(), disableStateChangeByCommit);
    this.disableRepositoryConfiguration = disableRepositoryConfiguration;
  }

}
