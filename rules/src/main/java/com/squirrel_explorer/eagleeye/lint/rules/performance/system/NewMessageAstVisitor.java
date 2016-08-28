package com.squirrel_explorer.eagleeye.lint.rules.performance.system;

import com.android.tools.lint.client.api.JavaParser;
import com.android.tools.lint.detector.api.JavaContext;
import com.squirrel_explorer.eagleeye.lint.types.base.BaseAstVisitor;
import com.squirrel_explorer.eagleeye.lint.utils.NodeUtils;

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
        JavaParser.ResolvedClass typeClass = NodeUtils.parseContainingClass(mContext, node);
        if (null != typeClass &&
                typeClass.isSubclassOf("android.os.Message", false)) {
            mContext.report(
                    NewMessageDetector.ISSUE,
                    mContext.getLocation(node),
                    "Please use Message.obtain() instead of new Message()"
            );
        }

        return super.visitConstructorInvocation(node);
    }
}
