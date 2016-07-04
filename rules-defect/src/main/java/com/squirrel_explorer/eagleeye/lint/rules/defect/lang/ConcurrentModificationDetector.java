package com.squirrel_explorer.eagleeye.lint.rules.defect.lang;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.squirrel_explorer.eagleeye.types.base.BaseJavaDetector;

/**
 * Created by squirrel-explorer on 16/3/27.
 */
public class ConcurrentModificationDetector extends BaseJavaDetector {
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

    public ConcurrentModificationDetector() {
        super(ConcurrentModificationAstVisitor.class);
    }
}
