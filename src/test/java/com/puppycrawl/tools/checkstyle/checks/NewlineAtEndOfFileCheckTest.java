////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2015 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////

package com.puppycrawl.tools.checkstyle.checks;

import static com.puppycrawl.tools.checkstyle.checks.NewlineAtEndOfFileCheck.MSG_KEY_NO_NEWLINE_EOF;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.puppycrawl.tools.checkstyle.BaseCheckTestSupport;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.api.LocalizedMessage;

public class NewlineAtEndOfFileCheckTest
    extends BaseCheckTestSupport {
    @Override
    protected DefaultConfiguration createCheckerConfig(
        Configuration config) {
        final DefaultConfiguration dc = new DefaultConfiguration("root");
        dc.addChild(config);
        return dc;
    }

    @Test
    public void testNewlineLfAtEndOfFile() throws Exception {
        final DefaultConfiguration checkConfig =
            createCheckConfig(NewlineAtEndOfFileCheck.class);
        checkConfig.addAttribute("lineSeparator", LineSeparatorOption.LF.toString());
        final String[] expected = ArrayUtils.EMPTY_STRING_ARRAY;
        verify(
            createChecker(checkConfig),
            getPath("InputNewlineLfAtEndOfFile.java"),
            expected);
    }

    @Test
    public void testNewlineCrlfAtEndOfFile() throws Exception {
        final DefaultConfiguration checkConfig =
            createCheckConfig(NewlineAtEndOfFileCheck.class);
        checkConfig.addAttribute("lineSeparator", LineSeparatorOption.CRLF.toString());
        final String[] expected = ArrayUtils.EMPTY_STRING_ARRAY;
        verify(
            createChecker(checkConfig),
            getPath("InputNewlineCrlfAtEndOfFile.java"),
            expected);
    }

    @Test
    public void testNewlineCrAtEndOfFile() throws Exception {
        final DefaultConfiguration checkConfig =
            createCheckConfig(NewlineAtEndOfFileCheck.class);
        checkConfig.addAttribute("lineSeparator", LineSeparatorOption.CR.toString());
        final String[] expected = ArrayUtils.EMPTY_STRING_ARRAY;
        verify(
            createChecker(checkConfig),
            getPath("InputNewlineCrAtEndOfFile.java"),
            expected);
    }

    @Test
    public void testAnyNewlineAtEndOfFile() throws Exception {
        final DefaultConfiguration checkConfig =
            createCheckConfig(NewlineAtEndOfFileCheck.class);
        checkConfig.addAttribute("lineSeparator", LineSeparatorOption.LF_CR_CRLF.toString());
        final String[] expected = ArrayUtils.EMPTY_STRING_ARRAY;
        verify(
            createChecker(checkConfig),
            getPath("InputNewlineCrlfAtEndOfFile.java"),
            expected);
        verify(
            createChecker(checkConfig),
            getPath("InputNewlineLfAtEndOfFile.java"),
            expected);
        verify(
            createChecker(checkConfig),
            getPath("InputNewlineCrAtEndOfFile.java"),
            expected);
    }

    @Test
    public void testNoNewlineLfAtEndOfFile() throws Exception {
        final DefaultConfiguration checkConfig =
            createCheckConfig(NewlineAtEndOfFileCheck.class);
        checkConfig.addAttribute("lineSeparator", LineSeparatorOption.LF.toString());
        final String[] expected = {
            "0: " + getCheckMessage(MSG_KEY_NO_NEWLINE_EOF),
        };
        verify(
            createChecker(checkConfig),
            getPath("InputNoNewlineAtEndOfFile.java"),
            expected);
    }

    @Test
    public void testNoNewlineAtEndOfFile() throws Exception {
        final DefaultConfiguration checkConfig =
            createCheckConfig(NewlineAtEndOfFileCheck.class);
        checkConfig.addAttribute("lineSeparator", LineSeparatorOption.LF_CR_CRLF.toString());
        final String[] expected = {
            "0: " + getCheckMessage(MSG_KEY_NO_NEWLINE_EOF),
        };
        verify(
            createChecker(checkConfig),
            getPath("InputNoNewlineAtEndOfFile.java"),
            expected);
    }

    @Test(expected = CheckstyleException.class)
    public void testSetLineSeparatorFailure()
        throws Exception {
        final DefaultConfiguration checkConfig =
            createCheckConfig(NewlineAtEndOfFileCheck.class);
        checkConfig.addAttribute("lineSeparator", "ct");
        createChecker(checkConfig);
    }

    @Test
    public void testEmptyFileFile() throws Exception {
        final DefaultConfiguration checkConfig =
            createCheckConfig(NewlineAtEndOfFileCheck.class);
        checkConfig.addAttribute("lineSeparator", LineSeparatorOption.LF.toString());
        final String[] expected = {
            "0: " + getCheckMessage(MSG_KEY_NO_NEWLINE_EOF),
        };
        verify(
            createChecker(checkConfig),
            getPath("InputEmptyFile.txt"),
            expected);
    }

    @Test
    public void testWrongFile() throws Exception {
        final DefaultConfiguration checkConfig = createCheckConfig(NewlineAtEndOfFileCheck.class);
        NewlineAtEndOfFileCheck check = new NewlineAtEndOfFileCheck();
        check.configure(checkConfig);
        File impossibleFile = new File("");
        Set<LocalizedMessage> messages = check.process(impossibleFile, Lists.newArrayList("txt"));
        assertTrue(messages.size() == 1);
        Iterator<LocalizedMessage> iter = messages.iterator();
        assertEquals(iter.next().getMessage(), "Unable to open ''.");
    }

    @Test
    public void testWrongSeparatorLength() throws Exception {
        NewlineAtEndOfFileCheck check = new NewlineAtEndOfFileCheck();
        final DefaultConfiguration checkConfig = createCheckConfig(NewlineAtEndOfFileCheck.class);
        check.configure(checkConfig);

        Method method = NewlineAtEndOfFileCheck.class
                .getDeclaredMethod("endsWithNewline", RandomAccessFile.class);
        method.setAccessible(true);
        RandomAccessFile file = mock(RandomAccessFile.class);
        when(file.length()).thenReturn(2000000L);
        try {
            method.invoke(new NewlineAtEndOfFileCheck(), file);
        }
        catch (InvocationTargetException ex) {
            assertTrue(ex.getCause() instanceof IOException);
            if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
                assertEquals(ex.getCause().getMessage(), "Unable to read 2 bytes, got 0");
            }
            else {
                assertEquals(ex.getCause().getMessage(), "Unable to read 1 bytes, got 0");
            }
        }
    }
}
