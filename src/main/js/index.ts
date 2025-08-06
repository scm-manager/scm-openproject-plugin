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

import {ConfigurationBinder as cfgBinder} from "@scm-manager/ui-components";
import RedmineGlobalConfiguration from "./RedmineGlobalConfiguration";
import RedmineRepositoryConfiguration from "./RedmineRepositoryConfiguration";
import {binder} from "@scm-manager/ui-extensions";
import RedmineCommitMessageIssueKeyValidatorConfig from "./RedmineCommitMessageIssueKeyValidatorConfig";

cfgBinder.bindGlobal("/redmine", "scm-redmine-plugin.config.link", "redmineConfig", RedmineGlobalConfiguration);

cfgBinder.bindRepositorySetting(
  "/redmine",
  "scm-redmine-plugin.config.link",
  "redmineConfig",
  RedmineRepositoryConfiguration
);

binder.bind("commitMessageChecker.validator.RedmineCommitMessageIssueKeyValidator", RedmineCommitMessageIssueKeyValidatorConfig);
