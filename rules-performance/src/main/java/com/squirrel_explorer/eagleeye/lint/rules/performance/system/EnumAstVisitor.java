package com.squirrel_explorer.eagleeye.lint.rules.performance.system;

import com.android.tools.lint.detector.api.JavaContext;
import com.eagleeye.types.base.BaseAstVisitor;

import lombok.ast.EnumDeclaration;

/**
 * Created by squirrel-explorer on 16/02/22.
 */
public class EnumAstVisitor extends BaseAstVisitor {
    public EnumAstVisitor(JavaContext context) {
        super(context);
    }

    @Override
    public boolean visitEnumDeclaration(EnumDeclaration node) {
        mContext.report(
                EnumDetector.ISSUE,
                mContext.getLocation(node),
                "Please avoid using enums"
        );

        return super.visitEnumDeclaration(node);
    }
}
