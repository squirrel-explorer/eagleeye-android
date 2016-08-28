package com.squirrel_explorer.eagleeye.lint.rules.performance.view;

import com.android.tools.lint.client.api.JavaParser;
import com.android.tools.lint.detector.api.JavaContext;
import com.squirrel_explorer.eagleeye.lint.types.base.BaseAstVisitor;
import com.squirrel_explorer.eagleeye.lint.utils.NodeUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import lombok.ast.ConstructorInvocation;
import lombok.ast.Node;

/**
 * Created by squirrel-explorer on 16/02/22.
 */
public class WrongAllocationAstVisitor extends BaseAstVisitor {
    public WrongAllocationAstVisitor(JavaContext context) {
        super(context);
    }

    // Methods with the limit when creating new objects
    private static final HashMap<String, HashSet<String>> OBSERVED_METHODS =
            new HashMap<String, HashSet<String>>();
    {
        OBSERVED_METHODS.put("android.view.View",
                new HashSet<String>(Arrays.asList(
                        "onMeasure",
                        "onLayout",
                        "onDraw",
                        "dispatchDraw",
                        "dispatchKeyEvent"
                )));
    }

    @Override
    public boolean visitConstructorInvocation(ConstructorInvocation node) {
        Node containingMethod = JavaContext.findSurroundingMethod(node);
        if (null == containingMethod) {
            return super.visitConstructorInvocation(node);
        }

        JavaParser.ResolvedMethod resolvedMethod = NodeUtils.parseResolvedMethod(mContext, containingMethod);
        if (null == resolvedMethod) {
            return super.visitConstructorInvocation(node);
        }
        JavaParser.ResolvedClass resolvedClass = resolvedMethod.getContainingClass();
        if (null == resolvedClass) {
            return super.visitConstructorInvocation(node);
        }

        for (String observedClass : OBSERVED_METHODS.keySet()) {
            if (resolvedClass.isSubclassOf(observedClass, false) &&
                    OBSERVED_METHODS.get(observedClass).contains(resolvedMethod.getName())) {
                mContext.report(
                        WrongAllocationDetector.ISSUE,
                        mContext.getLocation(node),
                        String.format("Please don't create new objects in %s : %s", resolvedClass.getName(), resolvedMethod.getName())
                );
            }
        }

        return super.visitConstructorInvocation(node);
    }
}
