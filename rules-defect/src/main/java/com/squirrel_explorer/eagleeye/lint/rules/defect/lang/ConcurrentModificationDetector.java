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

/**
 * Created by squirrel-explorer on 16/3/27.
 *
 * Function: Scan risks which may cause ConcurrentModificationException
 *
 * 本规则功能:检查代码中可能引起ConcurrentModificationException的写法
 */
public class ConcurrentModificationDetector extends Detector implements Detector.JavaScanner {
    public static final Issue ISSUE = Issue.create(
            "ConcurrentModificationDetector",
            "Modify contents of a collection when traversing it",
            "DONOT invoke add/remove of a collection when traversing it",
            Category.CORRECTNESS,
            7,
            Severity.ERROR,
            new Implementation(
                    ConcurrentModificationDetector.class,
                    Scope.JAVA_FILE_SCOPE));

    @Override
    public AstVisitor createJavaVisitor(@NonNull JavaContext context) {
        return new ConcurrentModificationAstVisitor(context);
    }
}
