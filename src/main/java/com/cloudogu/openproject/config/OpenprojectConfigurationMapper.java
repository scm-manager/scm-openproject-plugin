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

package com.cloudogu.openproject.config;

import com.cloudogu.openproject.Constants;
import com.google.common.annotations.VisibleForTesting;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import org.apache.commons.lang.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sonia.scm.api.v2.resources.HalAppenderMapper;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;

import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class OpenprojectConfigurationMapper extends HalAppenderMapper {

  @VisibleForTesting
  @SuppressWarnings("squid:S2068")
  static final String DUMMY_API_TOKEN = "__DUMMY__";

  @SuppressWarnings("java:S6813") // Cannot use constuctor injectiop with MapStruct
  @Inject
  private ScmPathInfoStore scmPathInfoStore;

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  public abstract OpenprojectConfigurationDto map(OpenprojectConfiguration configuration, @Context Repository repository);

  public abstract OpenprojectConfiguration map(OpenprojectConfigurationDto configurationDto, @Context OpenprojectConfiguration oldConfiguration);

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  public abstract OpenprojectGlobalConfigurationDto map(OpenprojectGlobalConfiguration configuration);

  public abstract OpenprojectGlobalConfiguration map(OpenprojectGlobalConfigurationDto configurationDto, @Context OpenprojectGlobalConfiguration oldConfiguration);

  @AfterMapping
  public void addLinks(OpenprojectGlobalConfiguration source, @MappingTarget OpenprojectGlobalConfigurationDto target) {
    Links.Builder linksBuilder = linkingTo().self(globalSelf());
    if (ConfigurationPermissions.write(Constants.NAME).isPermitted()) {
      linksBuilder.single(Link.link("update", globalUpdate()));

    }
    target.add(linksBuilder.build());
  }

  @AfterMapping
  public void replaceApiTokenWithDummy(@MappingTarget OpenprojectConfigurationDto target) {
    if (StringUtils.isNotEmpty(target.getApiToken())) {
      target.setApiToken(DUMMY_API_TOKEN);
    }
  }

  @AfterMapping
  public void restoreApiTokenOnDummy(@MappingTarget OpenprojectConfiguration target, @Context OpenprojectConfiguration oldConfiguration) {
    if (DUMMY_API_TOKEN.equals(target.getApiToken())) {
      target.setApiToken(oldConfiguration.getApiToken());
    }
  }

  private String globalSelf() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), OpenprojectConfigurationResource.class);
    return linkBuilder.method("getGlobalConfiguration").parameters().href();
  }

  private String globalUpdate() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), OpenprojectConfigurationResource.class);
    return linkBuilder.method("updateGlobalConfiguration").parameters().href();
  }


  @AfterMapping
  public void addLinks(OpenprojectConfiguration source, @MappingTarget OpenprojectConfigurationDto target, @Context Repository repository) {
    Links.Builder linksBuilder = linkingTo().self(self(repository));
    if (RepositoryPermissions.custom(Constants.NAME, repository).isPermitted()) {
      linksBuilder.single(Link.link("update", update(repository)));
    }
    target.add(linksBuilder.build());
  }

  private String self(Repository repository) {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), OpenprojectConfigurationResource.class);
    return linkBuilder.method("getConfiguration").parameters(repository.getNamespace(), repository.getName()).href();
  }

  private String update(Repository repository) {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), OpenprojectConfigurationResource.class);
    return linkBuilder.method("updateConfiguration").parameters(repository.getNamespace(), repository.getName()).href();
  }



}
