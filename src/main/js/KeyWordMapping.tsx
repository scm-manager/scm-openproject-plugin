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

import React, { FC, useState } from "react";
import { useTranslation } from "react-i18next";
import { InputField, AddButton, Help, Notification, Button } from "@scm-manager/ui-components";
import styled from "styled-components";

type Mapping = {
  status: string;
  keywords: string;
};

type MappingProps = {
  mapping: Mapping;
  remove: () => void;
  update: (mapping: Mapping) => void;
};

const VCenteredTd = styled.td`
  display: table-cell;
  vertical-align: middle !important;
`;

const MappingForm: FC<MappingProps> = ({ mapping, remove, update }) => {
  const [t] = useTranslation("plugins");

  const onStatusChange = (value: string) => {
    update({
      status: value,
      keywords: mapping.keywords
    });
  };

  const onKeywordsChange = (value: string) => {
    update({
      status: mapping.status,
      keywords: value
    });
  };

  return (
    <tr>
      <VCenteredTd>
        <InputField
          className="is-grouped"
          onChange={onStatusChange}
          value={mapping.status}
          placeholder={t("scm-redmine-plugin.config.mapping.status")}
        />
      </VCenteredTd>
      <VCenteredTd>
        <InputField
          className="is-grouped"
          onChange={onKeywordsChange}
          value={mapping.keywords}
          placeholder={t("scm-redmine-plugin.config.mapping.keywordsPlaceholder")}
        />
      </VCenteredTd>
      <VCenteredTd>
        <Button
          color="text"
          icon="trash"
          action={remove}
          title={t("scm-redmine-plugin.config.mapping.remove")}
          className="px-2"
        />
      </VCenteredTd>
    </tr>
  );
};

type Props = {
  mappings: Record<string, string>;
  onChange: (mappings: Record<string, string>) => void;
};

const AddMappingButton = styled(AddButton)`
  float: right;
`;

const convert = (mappings: Record<string, string>): Mapping[] => {
  return Object.keys(mappings).map(name => ({
    status: name,
    keywords: mappings[name]
  }));
};

const KeyWordMapping: FC<Props> = props => {
  const [t] = useTranslation("plugins");
  const [mappings, setMappings] = useState<Mapping[]>(convert(props.mappings));

  const onChange = (newMappings: Mapping[]) => {
    setMappings(newMappings);
    const record: Record<string, string> = {};
    newMappings.forEach(mapping => {
      if(mapping.status in record) {
        record[mapping.status] += ",".concat(mapping.keywords);
      } else {
        record[mapping.status] = mapping.keywords;
      }
    });
    props.onChange(record);
  };

  const addMapping = () => {
    onChange([...mappings, { status: "", keywords: "" }]);
  };

  const updateMapping = (index: number) => {
    return (mapping: Mapping) => {
      mappings[index] = mapping;
      onChange([...mappings]);
    };
  };

  const removeMapping = (index: number) => {
    return () => {
      mappings.splice(index, 1);
      onChange([...mappings]);
    };
  };

  return (
    <>
      <h3>
        {t("scm-redmine-plugin.config.mapping.title")}
        <Help message={t("scm-redmine-plugin.config.mapping.help")} />
      </h3>
      {!mappings || mappings.length === 0 ? (
        <Notification type="info">
          {t("scm-redmine-plugin.config.mapping.no-mapping")}
        </Notification>
        ) : (
        <table className="card-table table is-hoverable is-fullwidth">
          <thead>
            <tr>
              <th>
                {t("scm-redmine-plugin.config.mapping.status")}
                <Help message={t("scm-redmine-plugin.config.mapping.statusHelp")} />
              </th>
              <th>
                {t("scm-redmine-plugin.config.mapping.keywords")}
                <Help message={t("scm-redmine-plugin.config.mapping.keywordsHelp")} />
              </th>
              <th />
            </tr>
          </thead>
          <tbody>
            {mappings.map((mapping, idx) => (
              <MappingForm key={idx} mapping={mapping} remove={removeMapping(idx)} update={updateMapping(idx)} />
            ))}
          </tbody>
        </table>
      )}
      <AddMappingButton label={t("scm-redmine-plugin.config.mapping.add")} action={addMapping} />
    </>
  );
};

export default KeyWordMapping;
