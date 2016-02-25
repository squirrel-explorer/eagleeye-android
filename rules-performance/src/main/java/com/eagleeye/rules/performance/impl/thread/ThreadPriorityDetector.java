package com.eagleeye.rules.performance.impl.thread;

import com.android.annotations.NonNull;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

import lombok.ast.AstVisitor;

/**
 * Created by squirrel-explorer on 16/02/22.
 *
 * Function: Check all Thread objects created by new Thread(), and prompt warning
 * if they do not call Thread.setPriority() explicitly.
 *
 * 本规则功能：检查代码中用new Thread()创建新线程的地方，如果创建完成后未显式调用Thread.setPriority()，
 * 或者调用设置了较高优先级，则报警。
 */
public class ThreadPriorityDetector extends Detector implements Detector.JavaScanner {
    public static final Issue ISSUE = Issue.create(
            "ThreadPriorityDetector",
            "Set lower thread priority",
            "This app should set lower thread priority when creating new thread.",
            Category.PERFORMANCE,
            6,
            Severity.WARNING,
            new Implementation(
                    ThreadPriorityDetector.class,
                    Scope.JAVA_FILE_SCOPE));

    @Override
    public AstVisitor createJavaVisitor(@NonNull JavaContext context) {
        return new ThreadPriorityAstVisitor(context);
    }
}
