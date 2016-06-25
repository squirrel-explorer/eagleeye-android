package com.squirrel_explorer.eagleeye.types.base;

import com.android.tools.lint.detector.api.JavaContext;

import lombok.ast.ForwardingAstVisitor;

/**
 * Created by squirrel-explorer on 16/02/22.
 *
 * Base AST visitor, to be customized for more complex parsing
 */
public class BaseAstVisitor extends ForwardingAstVisitor {
    protected JavaContext mContext;

    public BaseAstVisitor(JavaContext context) {
        mContext = context;
    }
}
