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

import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.redmine.config.RedmineGlobalConfiguration;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.version.Version;

import jakarta.inject.Inject;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.Optional;

import static sonia.scm.version.Version.parse;

@Extension
public class RedmineV2GlobalConfigMigrationUpdateStep implements UpdateStep {

  public static final String STORE_NAME = "redmine";
  private final ConfigurationStoreFactory storeFactory;

  @Inject
  public RedmineV2GlobalConfigMigrationUpdateStep(ConfigurationStoreFactory storeFactory) {
    this.storeFactory = storeFactory;
  }

  @Override
  public void doUpdate() {
    Optional<V1RedmineGlobalConfiguration> optionalConfig = storeFactory.withType(V1RedmineGlobalConfiguration.class).withName(STORE_NAME).build().getOptional();
    if (isV1Config(optionalConfig)) {
      optionalConfig.ifPresent(
          v1RedmineConfig -> {
            RedmineGlobalConfiguration v2RedmineConfig = new RedmineGlobalConfiguration(
              v1RedmineConfig.getUrl(),
              v1RedmineConfig.getTextFormatting(),
              v1RedmineConfig.isAutoClose(),
              v1RedmineConfig.isUpdateIssues(),
              v1RedmineConfig.isDisableRepositoryConfiguration(),
              "",
              ""
            );
            storeFactory.withType(RedmineGlobalConfiguration.class).withName(STORE_NAME).build().set(v2RedmineConfig);
          }
        );
    }
  }

  private boolean isV1Config(Optional<V1RedmineGlobalConfiguration> optionalConfig) {
    if (optionalConfig.isPresent()) {
      try {
        return optionalConfig.get() instanceof V1RedmineGlobalConfiguration;
      } catch (ClassCastException e) {
        return true;
      }
    } else {
      return false;
    }
  }

  @Override
  public Version getTargetVersion() {
    return parse("2.0.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.redmine.config.global.xml";
  }

  @XmlRootElement(name = "redmine")
  @XmlAccessorType(XmlAccessType.FIELD)
  static class V1RedmineGlobalConfiguration extends RedmineGlobalConfiguration {
  }
}
