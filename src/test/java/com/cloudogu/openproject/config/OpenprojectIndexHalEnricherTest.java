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
import com.google.inject.util.Providers;
import jakarta.inject.Provider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.api.v2.resources.HalAppender;
import sonia.scm.api.v2.resources.HalEnricherContext;
import sonia.scm.api.v2.resources.ScmPathInfoStore;

import java.net.URI;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class OpenprojectIndexHalEnricherTest {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  @Mock
  private HalAppender appender;

  private OpenprojectIndexHalEnricher enricher;

  @Before
  public void setUp() {
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("https://scm-manager.org/scm/api/"));
    Provider<ScmPathInfoStore> scmPathInfoStoreProvider = Providers.of(scmPathInfoStore);
    enricher = new OpenprojectIndexHalEnricher(scmPathInfoStoreProvider);
  }

  @SubjectAware(
    username = "trillian",
    password = "secret",
    configuration = "classpath:com/cloudogu/openproject/shiro.ini"
  )
  @Test
  public void testEnrich() {
    enricher.enrich(HalEnricherContext.of(), appender);
    verify(appender).appendLink("openprojectConfig", "https://scm-manager.org/scm/api/v2/openproject/configuration/");
  }

}
