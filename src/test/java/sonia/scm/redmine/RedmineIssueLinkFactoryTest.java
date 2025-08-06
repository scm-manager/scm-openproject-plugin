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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RedmineIssueLinkFactoryTest {

  @Test
  void shouldCreateRedmineLink() {
    RedmineIssueLinkFactory issueLinkFactory = new RedmineIssueLinkFactory("http://h2g2/");
    String link = issueLinkFactory.createLink("#42");
    assertThat(link).isEqualTo("http://h2g2/issues/42");
  }

  @Test
  void shouldCreateRedmineLinkEventWithoutMissingSlash() {
    RedmineIssueLinkFactory issueLinkFactory = new RedmineIssueLinkFactory("http://h2g2");
    String link = issueLinkFactory.createLink("#42");
    assertThat(link).isEqualTo("http://h2g2/issues/42");
  }

}
