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

package com.cloudogu.openproject.commitmessagechecker;

import com.cloudogu.openproject.OpenprojectIssueMatcher;
import com.cloudogu.scm.commitmessagechecker.Context;
import com.cloudogu.scm.commitmessagechecker.InvalidCommitMessageException;
import com.cloudogu.scm.commitmessagechecker.Validator;
import com.google.common.base.Strings;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sonia.scm.ContextEntry;
import sonia.scm.plugin.Extension;
import sonia.scm.plugin.Requires;
import sonia.scm.util.GlobUtil;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

@Extension
@Requires("scm-commit-message-checker-plugin")
public class OpenprojectCommitMessageIssueKeyValidator implements Validator {

  private static final String DEFAULT_ERROR_MESSAGE = "The commit message doesn't contain a valid Openproject issue key.";
  private static final Pattern ISSUE_KEY_PATTERN = Pattern.compile(MessageFormat.format(".*{0}.*", new OpenprojectIssueMatcher().getKeyPattern().pattern()));

  @Override
  public boolean isApplicableMultipleTimes() {
    return false;
  }

  @Override
  public Optional<Class<?>> getConfigurationType() {
    return Optional.of(OpenprojectCommitMessageIssueKeyValidatorConfig.class);
  }

  @Override
  public void validate(Context context, String commitMessage) {
    OpenprojectCommitMessageIssueKeyValidatorConfig configuration = context.getConfiguration(OpenprojectCommitMessageIssueKeyValidatorConfig.class);
    String commitBranch = context.getBranch();

    if (shouldValidateBranch(configuration, commitBranch) && isInvalidCommitMessage(commitMessage)) {
      throw new InvalidCommitMessageException(
        ContextEntry.ContextBuilder.entity(context.getRepository()),
        DEFAULT_ERROR_MESSAGE
      );
    }
  }

  private boolean shouldValidateBranch(OpenprojectCommitMessageIssueKeyValidatorConfig configuration, String commitBranch) {
    if (Strings.isNullOrEmpty(commitBranch) || Strings.isNullOrEmpty(configuration.getBranches())) {
      return true;
    }
    return Arrays
      .stream(configuration.getBranches().split(","))
      .anyMatch(branch -> GlobUtil.matches(branch.trim(), commitBranch));
  }

  private boolean isInvalidCommitMessage(String commitMessage) {
    return !ISSUE_KEY_PATTERN.matcher(commitMessage).matches();
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  @XmlRootElement
  static class OpenprojectCommitMessageIssueKeyValidatorConfig {
    private String branches;
  }
}
