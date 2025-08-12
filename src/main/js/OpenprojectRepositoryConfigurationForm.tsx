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

import React from "react";
import { OpenprojectConfiguration } from "./types";
import { WithTranslation, withTranslation } from "react-i18next";
import { InputField, DropDown, Checkbox } from "@scm-manager/ui-components";
import KeyWordMapping from "./KeyWordMapping";

type Props = WithTranslation & {
  initialConfiguration: OpenprojectConfiguration;
  readOnly: boolean;
  onConfigurationChange: (p1: OpenprojectConfiguration, p2: boolean) => void;
};

class OpenprojectRepositoryConfigurationForm extends React.Component<Props, OpenprojectConfiguration> {
  constructor(props: Props) {
    super(props);
    this.state = {
      ...props.initialConfiguration
    };
  }

  configChangeHandler = (value: any, name: string) => {
    this.setState(
      {
        [name]: value
      },
      () =>
        this.props.onConfigurationChange(
          {
            ...this.state
          },
          true
        )
    );
  };

  keywordMappingChanged = (keywordMapping: Record<string, string>) => {
    this.configChangeHandler(keywordMapping, "keywordMapping");
  };

  render() {
    return (
      <div className="columns is-multiline">
        <div className="column is-full">{this.renderInputField("url")}</div>
        <div className="column is-full">{this.renderCheckbox("updateIssues")}</div>
        {this.state.updateIssues ? (
          <>
            <div className="column is-full">{this.renderInputField("apiToken", "password")}</div>
            <div className="column is-full">{this.renderCheckbox("autoClose")}</div>
            <div className="column is-full">{this.renderCheckbox("disableStateChangeByCommit")}</div>
            {this.state.autoClose ? (
              <div className="column is-full">
                <KeyWordMapping mappings={this.state.keywordMapping} onChange={this.keywordMappingChanged} />
              </div>
            ) : null}
          </>
        ) : null}
      </div>
    );
  }

  renderInputField = (name: string, type?: string) => {
    const { readOnly, t } = this.props;
    return (
      <InputField
        type={type}
        label={t("scm-openproject-plugin.config.form." + name)}
        helpText={t("scm-openproject-plugin.config.form." + name + "-helptext")}
        onChange={this.configChangeHandler}
        value={this.state[name]}
        name={name}
        disabled={readOnly}
      />
    );
  };

  renderCheckbox = (name: string) => {
    const { readOnly, t } = this.props;
    return (
      <Checkbox
        name={name}
        label={t("scm-openproject-plugin.config.form." + name)}
        helpText={t("scm-openproject-plugin.config.form." + name + "-helptext")}
        checked={this.state[name]}
        onChange={this.configChangeHandler}
        disabled={readOnly}
      />
    );
  };
}

export default withTranslation("plugins")(OpenprojectRepositoryConfigurationForm);
