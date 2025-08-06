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

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.issuetracker.IssueMatcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class RedmineIssueMatcher implements IssueMatcher {

  private static final Pattern KEY_PATTERN = Pattern.compile("(#\\d+)");

  @Override
  public String getKey(Matcher matcher) {
    return matcher.group(1);
  }

  @Override
  public Pattern getKeyPattern() {
    return KEY_PATTERN;
  }
}
