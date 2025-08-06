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

import React, { FC, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { InputField } from "@scm-manager/ui-components";

type Configuration = {
  branches?: string;
};

type ConfigProps = {
  configurationChanged: (newRuleConfiguration: Configuration, valid: boolean) => void;
};

const RedmineCommitMessageIssueKeyValidatorConfig: FC<ConfigProps> = ({ configurationChanged }) => {
  const [t] = useTranslation("plugins");
  const [branches, setBranches] = useState<string | undefined>();

  useEffect(() => configurationChanged({ branches }, true), []);

  const onBranchesChange = (value: string) => {
    setBranches(value);
    configurationChanged({ branches: value }, true);
  };

  return (
    <InputField
      value={branches}
      label={t("validator.RedmineCommitMessageIssueKeyValidator.branches.label")}
      helpText={t("validator.RedmineCommitMessageIssueKeyValidator.branches.helpText")}
      onChange={onBranchesChange}
    />
  );
};

export default RedmineCommitMessageIssueKeyValidatorConfig;
