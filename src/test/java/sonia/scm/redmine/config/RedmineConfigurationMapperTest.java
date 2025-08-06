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
public class RedmineConfigurationMapperTest {

  private URI baseUri = URI.create("http://example.com/base/");

  private URI expectedBaseUri;

  @Rule
  public ShiroRule shiro = new ShiroRule();

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ScmPathInfoStore scmPathInfoStore;

  @InjectMocks
  RedmineConfigurationMapperImpl mapper;

  @Before
  public void init() {
    when(scmPathInfoStore.get().getApiRestUri()).thenReturn(baseUri);
    expectedBaseUri = baseUri.resolve("v2/redmine/configuration/");
  }

  @Test
  @SubjectAware(username = "trillian",
    password = "secret",
    configuration = "classpath:sonia/scm/redmine/shiro.ini"
  )
  public void shouldMapAttributesToDto() {
    RedmineConfigurationDto dto = mapper.map(createConfiguration(), createRepository());
    assertEquals( "heartofgo.ld", dto.getUrl());
    assertEquals(TextFormatting.MARKDOWN, dto.getTextFormatting());
    assertTrue(dto.isAutoClose());
    assertFalse(dto.isUpdateIssues());
  }

  @Test
  @SubjectAware(username = "trillian",
    password = "secret",
    configuration = "classpath:sonia/scm/redmine/shiro.ini"
  )
  public void shouldAddHalLinksToDto() {
    RedmineConfigurationDto dto = mapper.map(createConfiguration(), createRepository());
    assertEquals(expectedBaseUri.toString() + "foo/bar", dto.getLinks().getLinkBy("self").get().getHref());
    assertEquals(expectedBaseUri.toString() + "foo/bar", dto.getLinks().getLinkBy("update").get().getHref());
  }

  @Test
  @SubjectAware(username = "unpriv",
    password = "secret",
    configuration = "classpath:sonia/scm/redmine/shiro.ini"
  )
  public void shouldNotAddUpdateLinkToDtoIfNotPermitted() {
    RedmineConfigurationDto dto = mapper.map(createConfiguration(), createRepository());
    assertFalse(dto.getLinks().getLinkBy("update").isPresent());
  }

  @Test
  public void shouldMapAttributesFromDto() {
    RedmineConfiguration configuration = mapper.map(createDto(), createConfiguration());
    assertEquals( "heartofgo.ld", configuration.getUrl());
    assertEquals(TextFormatting.MARKDOWN, configuration.getTextFormatting());
    assertTrue(configuration.isAutoClose());
    assertFalse(configuration.isUpdateIssues());
  }

  @Test
  @SubjectAware(username = "trillian",
    password = "secret",
    configuration = "classpath:sonia/scm/redmine/shiro.ini"
  )
  public void shouldMapGlobalConfigurationAttributesToDto() {
    RedmineGlobalConfigurationDto dto = mapper.map(createGlobalConfiguration());
    assertFalse(dto.isDisableRepositoryConfiguration());
  }

  @Test
  public void shouldMapGlobalConfigurationDtoAttributesFromDto() {
    RedmineGlobalConfiguration configuration = mapper.map(createGlobalConfigurationDto(), createGlobalConfiguration());
    assertFalse(configuration.isDisableRepositoryConfiguration());
  }

  @Test
  @SubjectAware(
    username = "trillian",
    password = "secret",
    configuration = "classpath:sonia/scm/redmine/shiro.ini"
  )
  public void shouldReplacePasswordAfterMappingOnGlobalDto() {
    RedmineGlobalConfigurationDto configuration = mapper.map(createGlobalConfiguration());
    assertEquals(RedmineConfigurationMapper.DUMMY_PASSWORD, configuration.getPassword());
  }

  @Test
  @SubjectAware(
    username = "trillian",
    password = "secret",
    configuration = "classpath:sonia/scm/redmine/shiro.ini"
  )
  public void shouldReplacePasswordAfterMappingDto() {
    RedmineConfigurationDto configuration = mapper.map(createConfiguration(), createRepository());
    assertEquals(RedmineConfigurationMapper.DUMMY_PASSWORD, configuration.getPassword());
  }

  @Test
  @SubjectAware(
    username = "trillian",
    password = "secret",
    configuration = "classpath:sonia/scm/redmine/shiro.ini"
  )
  public void shouldNotReplacePasswordAfterMappingDtoIfEmpty() {
    RedmineConfiguration redmineConfiguration = createConfiguration();
    redmineConfiguration.setPassword("");
    RedmineConfigurationDto configuration = mapper.map(redmineConfiguration, createRepository());
    assertEquals("", configuration.getPassword());
  }

  @Test
  @SubjectAware(
    username = "trillian",
    password = "secret",
    configuration = "classpath:sonia/scm/redmine/shiro.ini"
  )
  public void shouldRestorePasswordAfterMappingFromGlobalDto() {
    RedmineGlobalConfigurationDto dto = createGlobalConfigurationDto();
    dto.setPassword(RedmineConfigurationMapper.DUMMY_PASSWORD);

    RedmineGlobalConfiguration configuration = mapper.map(dto, createGlobalConfiguration());
    assertEquals("secret", configuration.getPassword());
  }

  @Test
  @SubjectAware(
    username = "trillian",
    password = "secret",
    configuration = "classpath:sonia/scm/redmine/shiro.ini"
  )
  public void shouldRestorePasswordAfterMappingFromDto() {
    RedmineConfigurationDto dto = createDto();
    dto.setPassword(RedmineConfigurationMapper.DUMMY_PASSWORD);

    RedmineConfiguration configuration = mapper.map(dto, createConfiguration());
    assertEquals("secret", configuration.getPassword());
  }

  private RedmineConfiguration createConfiguration() {
    return new RedmineConfiguration("heartofgo.ld",
      TextFormatting.MARKDOWN,
      true,
      false,
      "trillian",
      "secret",
      Collections.emptyMap(),
      false
    );
  }

  private RedmineConfigurationDto createDto() {
    return new RedmineConfigurationDto("heartofgo.ld",
      TextFormatting.MARKDOWN,
      true,
      false,
      "trillian",
      "secret"
      ,
      Collections.emptyMap(),
      false
    );
  }

  private RedmineGlobalConfiguration createGlobalConfiguration() {
    RedmineGlobalConfiguration configuration = new RedmineGlobalConfiguration();
    configuration.setUrl("");
    configuration.setUsername("trillian");
    configuration.setPassword("secret");
    configuration.setTextFormatting(TextFormatting.TEXTILE);
    configuration.setUpdateIssues(false);
    configuration.setDisableRepositoryConfiguration(false);
    return configuration;
  }

  private RedmineGlobalConfigurationDto createGlobalConfigurationDto() {
    RedmineGlobalConfigurationDto configuration = new RedmineGlobalConfigurationDto();
    configuration.setUrl("");
    configuration.setTextFormatting(TextFormatting.TEXTILE);
    configuration.setUpdateIssues(false);
    configuration.setDisableRepositoryConfiguration(false);
    return configuration;
  }

  private Repository createRepository() {
    return new Repository("42", "GIT", "foo", "bar");
  }
}
