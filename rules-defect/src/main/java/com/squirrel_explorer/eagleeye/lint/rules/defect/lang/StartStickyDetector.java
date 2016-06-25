package com.squirrel_explorer.eagleeye.lint.rules.defect.lang;

import com.android.annotations.NonNull;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

import lombok.ast.AstVisitor;

public class StartStickyDetector extends Detector implements Detector.JavaScanner {
    public static final Issue ISSUE = Issue.create(
            "StartStickyDetector",
            "return START_STICKY/START_STICKY_COMPATIBILITY in Service.onStartCommand()",
            "DONOT return START_STICKY/START_STICKY_COMPATIBILITY in onStartCommand() when implementing your own Service",
            Category.CORRECTNESS,
            3,
            Severity.WARNING,
            new Implementation(
                    StartStickyDetector.class,
                    Scope.JAVA_FILE_SCOPE));

    @Override
    public AstVisitor createJavaVisitor(@NonNull JavaContext context) {
        return new StartStickyAstVisitor(context);
    }
}
