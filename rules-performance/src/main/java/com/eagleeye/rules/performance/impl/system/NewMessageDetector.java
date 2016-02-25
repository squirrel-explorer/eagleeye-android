package com.eagleeye.rules.performance.impl.system;

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
 * Function: Check all Message objects, and prompt using Message.obtain()
 * instead of new Message()
 *
 * 本规则功能：检查代码中使用new Message()创建Message对象的地方，提示改用Message.obtain()
 */
public class NewMessageDetector extends Detector implements Detector.JavaScanner {
    public static final Issue ISSUE = Issue.create(
            "NewMessageDetector",
            "Use Message.obtain() to retrieve a Message object",
            "This app should use Message.obtain() instead of new Message().",
            Category.PERFORMANCE,
            5,
            Severity.WARNING,
            new Implementation(
                    NewMessageDetector.class,
                    Scope.JAVA_FILE_SCOPE));

    @Override
    public AstVisitor createJavaVisitor(@NonNull JavaContext context) {
        return new NewMessageAstVisitor(context);
    }
}
