/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.squirrel_explorer.eagleeye.lint;

import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.Issue;
import com.squirrel_explorer.eagleeye.lint.rules.api_checker.MissingApiChecker;
import com.squirrel_explorer.eagleeye.lint.rules.defect.lang.*;
import com.squirrel_explorer.eagleeye.lint.rules.performance.layout.BothWrapContentDetector;
import com.squirrel_explorer.eagleeye.lint.rules.performance.system.EnumDetector;
import com.squirrel_explorer.eagleeye.lint.rules.performance.system.NewMessageDetector;
import com.squirrel_explorer.eagleeye.lint.rules.performance.thread.ThreadPriorityDetector;
import com.squirrel_explorer.eagleeye.lint.rules.performance.view.WrongAllocationDetector;

import java.util.Arrays;
import java.util.List;

/**
 * The list of issues that will be checked when running <code>lint</code>.
 */
public class LintIssueRegistry extends IssueRegistry {
    @Override
    public List<Issue> getIssues() {
        return Arrays.asList(
                // Rules of defect
                ConcurrentModificationDetector.ISSUE,
                StartStickyDetector.ISSUE,
                GetRunningAppProcessesDetector.ISSUE,
                // Rules of performance
                EnumDetector.ISSUE,
                NewMessageDetector.ISSUE,
                BothWrapContentDetector.ISSUE,
                ThreadPriorityDetector.ISSUE,
                WrongAllocationDetector.ISSUE,
                // Api Missing Checker
                MissingApiChecker.ISSUE
        );
    }
}
