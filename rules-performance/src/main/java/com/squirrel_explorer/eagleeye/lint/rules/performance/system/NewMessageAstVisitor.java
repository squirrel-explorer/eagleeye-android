package com.squirrel_explorer.eagleeye.lint.rules.performance.system;

import com.android.tools.lint.client.api.JavaParser;
import com.android.tools.lint.detector.api.JavaContext;
import com.squirrel_explorer.eagleeye.types.base.BaseAstVisitor;

import lombok.ast.ConstructorInvocation;

/**
 * Created by squirrel-explorer on 16/02/22.
 */
public class NewMessageAstVisitor extends BaseAstVisitor {
    public NewMessageAstVisitor(JavaContext context) {
        super(context);
    }

    @Override
    public boolean visitConstructorInvocation(ConstructorInvocation node) {
        JavaParser.ResolvedNode resolvedNode = mContext.resolve(node);
        if (!(resolvedNode instanceof JavaParser.ResolvedMethod)) {
            return super.visitConstructorInvocation(node);
        }
        JavaParser.ResolvedMethod resolvedMethod = (JavaParser.ResolvedMethod)resolvedNode;

        JavaParser.ResolvedClass typeClass = resolvedMethod.getContainingClass();
        if (null != typeClass && typeClass.isSubclassOf("android.os.Message", false)) {
            mContext.report(
                    NewMessageDetector.ISSUE,
                    mContext.getLocation(node),
                    "Please use Message.obtain() instead of new Message()"
            );
        }

        return super.visitConstructorInvocation(node);
    }
}
