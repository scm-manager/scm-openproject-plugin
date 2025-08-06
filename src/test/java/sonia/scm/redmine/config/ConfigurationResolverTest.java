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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationResolverTest {

  private RedmineConfigStore store;

  private ConfigurationResolver resolver;

  private final Repository repository = RepositoryTestData.createHeartOfGold();

  @BeforeEach
  void setUpResolver() {
    store = new RedmineConfigStore(new InMemoryConfigurationStoreFactory());
    resolver = new ConfigurationResolver(store);
  }

  @Test
  void shouldReturnEmptyOptional() {
    Optional<RedmineConfiguration> configuration = resolver.resolve(repository);
    assertThat(configuration).isEmpty();
  }

  @Test
  void shouldResolveRepositoryConfiguration() {
    RedmineConfiguration configuration = new RedmineConfiguration();
    configuration.setUrl("https://issues.hitchhiker.com");
    store.storeConfiguration(configuration, repository);

    Optional<RedmineConfiguration> resolvedConfiguration = resolver.resolve(repository);
    assertThat(resolvedConfiguration).hasValueSatisfying(c -> {
      assertThat(c.getUrl()).isEqualTo("https://issues.hitchhiker.com");
    });
  }

  @Test
  void shouldResolveGlobalConfiguration() {
    RedmineGlobalConfiguration configuration = new RedmineGlobalConfiguration();
    configuration.setUrl("https://hitchhiker.com/issues");
    store.storeConfiguration(configuration);

    Optional<RedmineConfiguration> resolvedConfiguration = resolver.resolve(repository);
    assertThat(resolvedConfiguration).hasValueSatisfying(c -> {
      assertThat(c.getUrl()).isEqualTo("https://hitchhiker.com/issues");
    });
  }

  @Test
  void shouldPreferRepositoryConfiguration() {
    RedmineGlobalConfiguration globalConfiguration = new RedmineGlobalConfiguration();
    globalConfiguration.setUrl("https://hitchhiker.com/issues");
    store.storeConfiguration(globalConfiguration);

    RedmineConfiguration repositoryConfiguration = new RedmineConfiguration();
    repositoryConfiguration.setUrl("https://issues.hitchhiker.com");
    store.storeConfiguration(repositoryConfiguration, repository);

    Optional<RedmineConfiguration> resolvedConfiguration = resolver.resolve(repository);
    assertThat(resolvedConfiguration).hasValueSatisfying(c -> {
      assertThat(c.getUrl()).isEqualTo("https://issues.hitchhiker.com");
    });
  }

  @Test
  void shouldUseGlobalConfigurationIfRepositoryConfigurationIsDisabled() {
    RedmineGlobalConfiguration globalConfiguration = new RedmineGlobalConfiguration();
    globalConfiguration.setUrl("https://hitchhiker.com/issues");
    globalConfiguration.setDisableRepositoryConfiguration(true);
    store.storeConfiguration(globalConfiguration);

    RedmineConfiguration repositoryConfiguration = new RedmineConfiguration();
    repositoryConfiguration.setUrl("https://issues.hitchhiker.com");
    store.storeConfiguration(repositoryConfiguration, repository);

    Optional<RedmineConfiguration> resolvedConfiguration = resolver.resolve(repository);
    assertThat(resolvedConfiguration).hasValueSatisfying(c -> {
      assertThat(c.getUrl()).isEqualTo("https://hitchhiker.com/issues");
    });
  }

  @Test
  void shouldReturnEmptyIfRepositoryConfigurationIsDisabledAndGlobalIsNotValid() {
    RedmineGlobalConfiguration globalConfiguration = new RedmineGlobalConfiguration();
    globalConfiguration.setDisableRepositoryConfiguration(true);
    store.storeConfiguration(globalConfiguration);

    Optional<RedmineConfiguration> resolvedConfiguration = resolver.resolve(repository);
    assertThat(resolvedConfiguration).isEmpty();
  }


}
