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

package sonia.scm.redmine.update;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.redmine.config.RedmineGlobalConfiguration;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static sonia.scm.redmine.config.TextFormatting.TEXTILE;
import static sonia.scm.redmine.update.RedmineV2GlobalConfigMigrationUpdateStep.*;

@ExtendWith(MockitoExtension.class)
class RedmineV2GlobalConfigMigrationUpdateStepTest {

  RedmineV2GlobalConfigMigrationUpdateStep updateStep;

  private InMemoryConfigurationStoreFactory storeFactory = new InMemoryConfigurationStoreFactory();

  @BeforeEach
  void initUpdateStep() {
    updateStep = new RedmineV2GlobalConfigMigrationUpdateStep(storeFactory);
  }

  @Nested
  class WithExistingV1Config {

    @BeforeEach
    void createRedmineV1XMLInMemory() {
      V1RedmineGlobalConfiguration redmineGlobalConfiguration = new V1RedmineGlobalConfiguration();
      redmineGlobalConfiguration.setUrl("test.de");
      redmineGlobalConfiguration.setPassword("1234");
      redmineGlobalConfiguration.setAutoClose(false);
      redmineGlobalConfiguration.setUsername("tester");
      redmineGlobalConfiguration.setTextFormatting(TEXTILE);
      redmineGlobalConfiguration.setDisableRepositoryConfiguration(false);
      redmineGlobalConfiguration.setUpdateIssues(true);
      storeFactory.withType(V1RedmineGlobalConfiguration.class).withName("redmine").build().set(redmineGlobalConfiguration);
    }

    @Test
    void shouldMigrateGlobalConfiguration() {
      updateStep.doUpdate();
      ConfigurationStore<RedmineGlobalConfiguration> testStore = storeFactory.get("redmine",null);
      RedmineGlobalConfiguration redmineGlobalConfiguration = testStore.get();
      assertThat(redmineGlobalConfiguration.getUrl()).isEqualToIgnoringCase("test.de");
      assertThat(redmineGlobalConfiguration.isAutoClose()).isFalse();
      assertThat(redmineGlobalConfiguration.getTextFormatting()).isEqualTo(TEXTILE);
      assertThat(redmineGlobalConfiguration.isDisableRepositoryConfiguration()).isFalse();
      assertThat(redmineGlobalConfiguration.isUpdateIssues()).isTrue();

      //Username and password does not exist in V1 and should not be migrated
      assertThat(redmineGlobalConfiguration.getUsername()).isEmpty();
      assertThat(redmineGlobalConfiguration.getPassword()).isEmpty();
    }
  }

  @Nested
  class WithExistingV2Config {
    @BeforeEach
    void createRedmineV2XMLInMemory() {
      RedmineGlobalConfiguration globalConfiguration = new RedmineGlobalConfiguration();
      storeFactory.withType(RedmineGlobalConfiguration.class).withName("redmine").build().set(globalConfiguration);
    }

    @Test
    void shouldNotFailForExistingV2Config() {
      updateStep.doUpdate();
    }
  }

  @Nested
  class WithoutAnyConfig {
    @BeforeEach
    void createRedmineV2XMLInMemory() {
    }

    @Test
    void shouldNotFailForMissingConfig() {
      updateStep.doUpdate();
    }
  }
}
