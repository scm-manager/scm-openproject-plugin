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
import static org.junit.jupiter.api.Assertions.*;

class IdsTest {

  @Test
  void shouldRemoveHashPrefix() {
    assertThat(Ids.parse("#123")).isEqualTo("123");
  }

  @Test
  void shouldParseAsInt() {
    assertThat(Ids.parseAsInt("#123")).isEqualTo(123);
  }

  @Test
  void shouldThrowIllegalArgumentExceptionOnNonRedmineId() {
    assertThrows(IllegalArgumentException.class, () -> Ids.parse("123"));
  }

}
