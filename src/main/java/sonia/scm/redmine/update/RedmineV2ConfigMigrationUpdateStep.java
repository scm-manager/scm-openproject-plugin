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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.redmine.config.RedmineConfigStore;
import sonia.scm.redmine.config.RedmineConfiguration;
import sonia.scm.redmine.config.TextFormatting;
import sonia.scm.update.V1Properties;
import sonia.scm.update.V1PropertyDAO;
import sonia.scm.version.Version;

import jakarta.inject.Inject;

import java.util.Collections;

import static sonia.scm.update.V1PropertyReader.REPOSITORY_PROPERTY_READER;
import static sonia.scm.version.Version.parse;

@Extension
public class RedmineV2ConfigMigrationUpdateStep implements UpdateStep {

  private static final Logger LOG = LoggerFactory.getLogger(RedmineV2ConfigMigrationUpdateStep.class);

  private final V1PropertyDAO v1PropertyDAO;
  private final RedmineConfigStore configStore;

  @Inject
  public RedmineV2ConfigMigrationUpdateStep(V1PropertyDAO v1PropertyDAO, RedmineConfigStore configStore) {
    this.v1PropertyDAO = v1PropertyDAO;
    this.configStore = configStore;
  }

  @Override
  public void doUpdate() {
    v1PropertyDAO
      .getProperties(REPOSITORY_PROPERTY_READER)
      .havingAnyOf("redmine.url", "redmine.text-formatting", "redmine.auto-close", "redmine.update-issues")
      .forEachEntry((key, properties) -> configStore.storeConfiguration(buildConfig(key, properties), key));
  }

  private RedmineConfiguration buildConfig(String repositoryId, V1Properties properties) {
    LOG.debug("migrating repository specific redmine configuration for repository id {}", repositoryId);
    return new RedmineConfiguration(
      properties.get("redmine.url"),
      properties.getEnum("redmine.text-formatting", TextFormatting.class).orElse(null),
      properties.getBoolean("redmine.auto-close").orElse(false),
      properties.getBoolean("redmine.update-issues").orElse(false),
      "",
      "",
      Collections.emptyMap(),
      false
    );
  }

  @Override
  public Version getTargetVersion() {
    return parse("2.0.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.redmine.config.repository.xml";
  }
}
