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

public class GetRunningAppProcessesDetector extends Detector implements Detector.JavaScanner {
    public static final Issue ISSUE = Issue.create(
            "GetRunningAppProcessesDetector",
            "Avoid invoking ActivityManager.getRunningAppProcesses()",
            "Since ActivityManager.getRunningAppProcesses() may consume too much resources, and it is not applicable in Android 5.1 above, we should avoid using it.",
            Category.CORRECTNESS,
            3,
            Severity.WARNING,
            new Implementation(
                    GetRunningAppProcessesDetector.class,
                    Scope.JAVA_FILE_SCOPE));

    @Override
    public AstVisitor createJavaVisitor(@NonNull JavaContext context) {
        return new GetRunningAppProcessesAstVisitor(context);
    }
}
