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

import com.cloudogu.scm.commitmessagechecker.Context;
import com.cloudogu.scm.commitmessagechecker.InvalidCommitMessageException;
import org.junit.jupiter.api.Test;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;

import static com.cloudogu.openproject.commitmessagechecker.OpenprojectCommitMessageIssueKeyValidator.OpenprojectCommitMessageIssueKeyValidatorConfig;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OpenprojectCommitMessageIssueKeyValidatorTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();
  private final OpenprojectCommitMessageIssueKeyValidator validator = new OpenprojectCommitMessageIssueKeyValidator();

  @Test
  void shouldValidateSuccessfully() {
    OpenprojectCommitMessageIssueKeyValidatorConfig config = new OpenprojectCommitMessageIssueKeyValidatorConfig();
    Context context = new Context(REPOSITORY, "", config);

    validator.validate(context, "valid commit by trillian #42");
  }

  @Test
  void shouldFailOnMissingIssueKey() {
    OpenprojectCommitMessageIssueKeyValidatorConfig config = new OpenprojectCommitMessageIssueKeyValidatorConfig();
    Context context = new Context(REPOSITORY, "master", config);

    assertThrows(InvalidCommitMessageException.class,
      () -> validator.validate(context, "invalid commit by trillian")
    );
  }

  @Test
  void shouldNotValidateIfBranchNotMatch() {
    OpenprojectCommitMessageIssueKeyValidatorConfig config = new OpenprojectCommitMessageIssueKeyValidatorConfig();
    config.setBranches("master");
    Context context = new Context(REPOSITORY, "develop", config);

    validator.validate(context, "invalid commit by trillian");
  }

}
