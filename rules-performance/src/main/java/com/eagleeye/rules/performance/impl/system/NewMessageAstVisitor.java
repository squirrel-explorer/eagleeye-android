package com.eagleeye.rules.performance.impl.system;

import com.android.tools.lint.detector.api.JavaContext;
import com.eagleeye.types.base.BaseAstVisitor;

import lombok.ast.ConstructorInvocation;
import lombok.ast.TypeReference;

/**
 * Created by squirrel-explorer on 16/02/22.
 */
public class NewMessageAstVisitor extends BaseAstVisitor {
    public NewMessageAstVisitor(JavaContext context) {
        super(context);
    }

    @Override
    public boolean visitConstructorInvocation(ConstructorInvocation node) {
        TypeReference typeRef = node.astTypeReference();
        if (null != typeRef && "Message".equals(typeRef.getTypeName())) {
            mContext.report(
                    NewMessageDetector.ISSUE,
                    mContext.getLocation(node),
                    "Please use Message.obtain() instead of new Message()"
            );
        }

        return super.visitConstructorInvocation(node);
    }
}
