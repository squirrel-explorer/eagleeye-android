package com.squirrel_explorer.eagleeye.lint.rules.performance.view;

import com.android.tools.lint.client.api.JavaParser;
import com.android.tools.lint.detector.api.JavaContext;
import com.squirrel_explorer.eagleeye.types.base.BaseAstVisitor;
import com.squirrel_explorer.eagleeye.utils.NodeUtils;

import java.util.Arrays;
import java.util.List;

import lombok.ast.ConstructorInvocation;

/**
 * Created by squirrel-explorer on 16/02/22.
 */
public class WrongAllocationAstVisitor extends BaseAstVisitor {
    public WrongAllocationAstVisitor(JavaContext context) {
        super(context);
    }

    // Methods with the limit on creating new objects
    // (currently only on some methods of View)
    private static final List<String> methodsObserved = Arrays.asList(
            "onMeasure",
            "onLayout",
            "onDraw"
    );

    @Override
    public boolean visitConstructorInvocation(ConstructorInvocation node) {
        JavaParser.ResolvedMethod method = NodeUtils.findEnclosingMethod(mContext, node);
        if (null != method) {
            JavaParser.ResolvedClass clazz = method.getContainingClass();
            if (null != clazz && clazz.isSubclassOf("android.view.View", false) &&
                    methodsObserved.contains(method.getName())) {
                mContext.report(
                        WrongAllocationDetector.ISSUE,
                        mContext.getLocation(node),
                        String.format("Please don't create new objects in %s : %s", clazz.getName(), method.getName())
                );
            }
        }

        return super.visitConstructorInvocation(node);
    }
}
