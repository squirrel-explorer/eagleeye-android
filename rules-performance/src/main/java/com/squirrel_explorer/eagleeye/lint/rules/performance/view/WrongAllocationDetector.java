package com.squirrel_explorer.eagleeye.lint.rules.performance.view;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.squirrel_explorer.eagleeye.types.base.BaseJavaDetector;

/**
 * Created by squirrel-explorer on 16/02/22.
 *
 * Function: In frequently used methods, such as onMeasure() / onLayut() / onDraw()
 * of View, prompt avoiding creation of new objects
 *
 * 本规则功能：在View的onMeasure()、onLayout()、onDraw()等可能被频繁调用的函数中，避免new对象
 */
public class WrongAllocationDetector extends BaseJavaDetector {
    public static final Issue ISSUE = Issue.create(
            "WrongAllocationDetector",
            "Avoid construct new objects in frequently called methods",
            "This app should avoid construct new objects in frequently called methods.",
            Category.PERFORMANCE,
            6,
            Severity.WARNING,
            new Implementation(
                    WrongAllocationDetector.class,
                    Scope.JAVA_FILE_SCOPE));

    public WrongAllocationDetector() {
        super(WrongAllocationAstVisitor.class);
    }
}
