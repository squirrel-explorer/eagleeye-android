package com.squirrel_explorer.eagleeye.lint.rules.performance.system;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.squirrel_explorer.eagleeye.types.base.BaseJavaDetector;

/**
 * Created by squirrel-explorer on 16/02/22.
 *
 * Function: Prompt avoiding use of enums
 *
 * 本规则功能：提示尽量避免使用枚举
 */
public class EnumDetector extends BaseJavaDetector {
    public static final Issue ISSUE = Issue.create(
            "EnumDetector",
            "Use constants instead of enums",
            "This app should use constants instead of enums.",
            Category.PERFORMANCE,
            2,
            Severity.WARNING,
            new Implementation(
                    EnumDetector.class,
                    Scope.JAVA_FILE_SCOPE));

    public EnumDetector() {
        super(EnumAstVisitor.class);
    }
}
