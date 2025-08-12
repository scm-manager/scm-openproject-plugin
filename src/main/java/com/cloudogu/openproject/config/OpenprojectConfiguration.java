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
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sonia.scm.Validateable;
import sonia.scm.util.Util;
import sonia.scm.xml.XmlEncryptionAdapter;

import java.util.Collections;
import java.util.Map;

/**
 * @author Sebastian Sdorra
 * @author Marvin Froeder marvin_at_marvinformatics_dot_com
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@XmlRootElement(name = "openprojectConfig")
@XmlAccessorType(XmlAccessType.FIELD)
public class OpenprojectConfiguration implements Validateable {

  private String url;
  private boolean autoClose;
  private boolean updateIssues;
  @XmlJavaTypeAdapter(XmlEncryptionAdapter.class)
  private String apiToken;
  private Map<String, String> keywordMapping;
  private boolean disableStateChangeByCommit;

  public boolean isAutoCloseEnabled() {
    return isUpdateIssuesEnabled() && autoClose;
  }

  public boolean isUpdateIssuesEnabled() {
    return isValid() && updateIssues;
  }

  public Map<String, String> getKeywordMapping() {
    if (keywordMapping == null) {
      return Collections.emptyMap();
    }
    return keywordMapping;
  }

  @Override
  public boolean isValid() {
    return Util.isNotEmpty(url);
  }

}
