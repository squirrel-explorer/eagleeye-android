package com.squirrel_explorer.eagleeye.lint.rules.defect.lang;

import com.android.tools.lint.client.api.JavaParser;
import com.android.tools.lint.detector.api.JavaContext;
import com.squirrel_explorer.eagleeye.lint.types.base.BaseAstVisitor;
import com.squirrel_explorer.eagleeye.lint.utils.NodeUtils;

import lombok.ast.Expression;
import lombok.ast.MethodDeclaration;
import lombok.ast.Return;
import lombok.ast.Statement;

public class StartStickyAstVisitor extends BaseAstVisitor {
    // Signature of onStartCommand()
    private static final String ONSTARTCOMMAND_SIGNATURE = "public int onStartCommand(android.content.Intent, int, int) ";

    public StartStickyAstVisitor(JavaContext context) {
        super(context);
    }

    @Override
    public boolean visitMethodDeclaration(MethodDeclaration node) {
        JavaParser.ResolvedMethod resolvedMethod = NodeUtils.parseResolvedMethod(mContext, node);
        if (null == resolvedMethod) {
            return super.visitMethodDeclaration(node);
        }

        // The containing class must be subclass of android.app.Service
        JavaParser.ResolvedClass resolvedClass = resolvedMethod.getContainingClass();
        if (!resolvedClass.isSubclassOf("android.app.Service", false)) {
            return super.visitMethodDeclaration(node);
        }

        // Only handle Service.onStartCommand()
        if (!ONSTARTCOMMAND_SIGNATURE.equals(resolvedMethod.getSignature())) {
            return super.visitMethodDeclaration(node);
        }

        // Get the RETURN statement of onStartCommand()
        Statement lastStatement = node.astBody().astContents().last();
        if (!(lastStatement instanceof Return)) {
            return super.visitMethodDeclaration(node);
        }
        Return returnStatement = (Return)lastStatement;
        // We make a very simple check here. That is, check if the RETURN value contains
        // START_STICKY. Because the RETURN value may be very complex expression, and it
        // may be too difficult to calculate.
        Expression returnValue = returnStatement.astValue();
        if (null != returnValue &&
                returnValue.toString().contains("START_STICKY")) {
            mContext.report(
                    StartStickyDetector.ISSUE,
                    mContext.getLocation(returnStatement),
                    "Please DO NOT return START_STICKY/START_STICKY_COMPATIBILITY in onStartCommand() when implementing your own Service.");
        }

        return super.visitMethodDeclaration(node);
    }
}
