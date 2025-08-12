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

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.Repository;

import java.net.URI;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("squid:S2068")
public class OpenprojectConfigurationMapperTest {

  private URI baseUri = URI.create("http://example.com/base/");

  private URI expectedBaseUri;

  @Rule
  public ShiroRule shiro = new ShiroRule();

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ScmPathInfoStore scmPathInfoStore;

  @InjectMocks
  OpenprojectConfigurationMapperImpl mapper;

  @Before
  public void init() {
    when(scmPathInfoStore.get().getApiRestUri()).thenReturn(baseUri);
    expectedBaseUri = baseUri.resolve("v2/openproject/configuration/");
  }

  @Test
  @SubjectAware(username = "trillian",
    password = "secret",
    configuration = "classpath:com/cloudogu/openproject/shiro.ini"
  )
  public void shouldMapAttributesToDto() {
    OpenprojectConfigurationDto dto = mapper.map(createConfiguration(), createRepository());
    assertEquals( "heartofgo.ld", dto.getUrl());
    assertTrue(dto.isAutoClose());
    assertFalse(dto.isUpdateIssues());
  }

  @Test
  @SubjectAware(username = "trillian",
    password = "secret",
    configuration = "classpath:com/cloudogu/openproject/shiro.ini"
  )
  public void shouldAddHalLinksToDto() {
    OpenprojectConfigurationDto dto = mapper.map(createConfiguration(), createRepository());
    assertEquals(expectedBaseUri.toString() + "foo/bar", dto.getLinks().getLinkBy("self").get().getHref());
    assertEquals(expectedBaseUri.toString() + "foo/bar", dto.getLinks().getLinkBy("update").get().getHref());
  }

  @Test
  @SubjectAware(username = "unpriv",
    password = "secret",
    configuration = "classpath:com/cloudogu/openproject/shiro.ini"
  )
  public void shouldNotAddUpdateLinkToDtoIfNotPermitted() {
    OpenprojectConfigurationDto dto = mapper.map(createConfiguration(), createRepository());
    assertFalse(dto.getLinks().getLinkBy("update").isPresent());
  }

  @Test
  public void shouldMapAttributesFromDto() {
    OpenprojectConfiguration configuration = mapper.map(createDto(), createConfiguration());
    assertEquals( "heartofgo.ld", configuration.getUrl());
    assertTrue(configuration.isAutoClose());
    assertFalse(configuration.isUpdateIssues());
  }

  @Test
  @SubjectAware(username = "trillian",
    password = "secret",
    configuration = "classpath:com/cloudogu/openproject/shiro.ini"
  )
  public void shouldMapGlobalConfigurationAttributesToDto() {
    OpenprojectGlobalConfigurationDto dto = mapper.map(createGlobalConfiguration());
    assertFalse(dto.isDisableRepositoryConfiguration());
  }

  @Test
  public void shouldMapGlobalConfigurationDtoAttributesFromDto() {
    OpenprojectGlobalConfiguration configuration = mapper.map(createGlobalConfigurationDto(), createGlobalConfiguration());
    assertFalse(configuration.isDisableRepositoryConfiguration());
  }

  @Test
  @SubjectAware(
    username = "trillian",
    password = "secret",
    configuration = "classpath:com/cloudogu/openproject/shiro.ini"
  )
  public void shouldReplaceApiTokenAfterMappingOnGlobalDto() {
    OpenprojectGlobalConfigurationDto configuration = mapper.map(createGlobalConfiguration());
    assertEquals(OpenprojectConfigurationMapper.DUMMY_API_TOKEN, configuration.getApiToken());
  }

  @Test
  @SubjectAware(
    username = "trillian",
    password = "secret",
    configuration = "classpath:com/cloudogu/openproject/shiro.ini"
  )
  public void shouldReplaceApiTokenAfterMappingDto() {
    OpenprojectConfigurationDto configuration = mapper.map(createConfiguration(), createRepository());
    assertEquals(OpenprojectConfigurationMapper.DUMMY_API_TOKEN, configuration.getApiToken());
  }

  @Test
  @SubjectAware(
    username = "trillian",
    password = "secret",
    configuration = "classpath:com/cloudogu/openproject/shiro.ini"
  )
  public void shouldNotReplaceApiTokenAfterMappingDtoIfEmpty() {
    OpenprojectConfiguration openprojectConfiguration = createConfiguration();
    openprojectConfiguration.setApiToken("");
    OpenprojectConfigurationDto configuration = mapper.map(openprojectConfiguration, createRepository());
    assertEquals("", configuration.getApiToken());
  }

  @Test
  @SubjectAware(
    username = "trillian",
    password = "secret",
    configuration = "classpath:com/cloudogu/openproject/shiro.ini"
  )
  public void shouldRestoreApiTokenAfterMappingFromGlobalDto() {
    OpenprojectGlobalConfigurationDto dto = createGlobalConfigurationDto();
    dto.setApiToken(OpenprojectConfigurationMapper.DUMMY_API_TOKEN);

    OpenprojectGlobalConfiguration configuration = mapper.map(dto, createGlobalConfiguration());
    assertEquals("secret", configuration.getApiToken());
  }

  @Test
  @SubjectAware(
    username = "trillian",
    password = "secret",
    configuration = "classpath:com/cloudogu/openproject/shiro.ini"
  )
  public void shouldRestoreApiTokenAfterMappingFromDto() {
    OpenprojectConfigurationDto dto = createDto();
    dto.setApiToken(OpenprojectConfigurationMapper.DUMMY_API_TOKEN);

    OpenprojectConfiguration configuration = mapper.map(dto, createConfiguration());
    assertEquals("secret", configuration.getApiToken());
  }

  private OpenprojectConfiguration createConfiguration() {
    return new OpenprojectConfiguration("heartofgo.ld",
      true,
      false,
      "secret",
      Collections.emptyMap(),
      false
    );
  }

  private OpenprojectConfigurationDto createDto() {
    return new OpenprojectConfigurationDto("heartofgo.ld",
      true,
      false,
      "secret"
      ,
      Collections.emptyMap(),
      false
    );
  }

  private OpenprojectGlobalConfiguration createGlobalConfiguration() {
    OpenprojectGlobalConfiguration configuration = new OpenprojectGlobalConfiguration();
    configuration.setUrl("");
    configuration.setApiToken("secret");
    configuration.setUpdateIssues(false);
    configuration.setDisableRepositoryConfiguration(false);
    return configuration;
  }

  private OpenprojectGlobalConfigurationDto createGlobalConfigurationDto() {
    OpenprojectGlobalConfigurationDto configuration = new OpenprojectGlobalConfigurationDto();
    configuration.setUrl("");
    configuration.setUpdateIssues(false);
    configuration.setDisableRepositoryConfiguration(false);
    return configuration;
  }

  private Repository createRepository() {
    return new Repository("42", "GIT", "foo", "bar");
  }
}
