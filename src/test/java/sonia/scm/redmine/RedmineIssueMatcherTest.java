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

package sonia.scm.redmine;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link RedmineIssueMatcher}.
 *
 * @author Sebastian Sdorra <sebastian.sdorra@triology.de>
 */
@RunWith(MockitoJUnitRunner.class)
public class RedmineIssueMatcherTest {

    @InjectMocks
    private RedmineIssueMatcher issueMatcher;

    /**
     * Tests {@link RedmineIssueMatcher}.
     */
    @Test
    public void testIssueMatching() {
        assertIssueMatches("#42", "fixed bug #42, by adding some stuff");
        assertIssueMatches("#422", "#422 should now be fixed");
        assertIssueMatches("#4222", "fixed #4222");
        assertIssueMatches("#4222", "fixed #4222");
        assertIssueMatches("#42", "old style issue id (#42)");
    }

    private void assertIssueMatches(String expectedKey, String message){
        Pattern pattern = issueMatcher.getKeyPattern();
        Matcher matcher = pattern.matcher(message);
        assertTrue(matcher.find());
        assertEquals(expectedKey, issueMatcher.getKey(matcher));
    }

}
