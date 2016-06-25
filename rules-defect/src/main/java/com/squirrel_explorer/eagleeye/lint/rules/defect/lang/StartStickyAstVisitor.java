package com.squirrel_explorer.eagleeye.lint.rules.defect.lang;

import com.android.tools.lint.client.api.JavaParser;
import com.android.tools.lint.detector.api.JavaContext;
import com.squirrel_explorer.eagleeye.types.base.BaseAstVisitor;

import java.util.Iterator;

import lombok.ast.Expression;
import lombok.ast.MethodDeclaration;
import lombok.ast.Return;
import lombok.ast.Statement;
import lombok.ast.StrictListAccessor;
import lombok.ast.VariableDefinition;

public class StartStickyAstVisitor extends BaseAstVisitor {
    // Parameter types of onStartCommand()
    private static final String[] ONSTARTCOMMAND_PARAM_TYPES = {
            "android.content.Intent",
            "int",
            "int"
    };

    public StartStickyAstVisitor(JavaContext context) {
        super(context);
    }

    @Override
    public boolean visitMethodDeclaration(MethodDeclaration node) {
        // Only handle Service.onStartCommand()
        if (!"onStartCommand".equals(node.astMethodName().astValue())) {
            return super.visitMethodDeclaration(node);
        }

        JavaParser.ResolvedNode resolvedNode = mContext.resolve(node);
        if (!(resolvedNode instanceof JavaParser.ResolvedMethod)) {
            return super.visitMethodDeclaration(node);
        }
        JavaParser.ResolvedMethod resolvedMethod = (JavaParser.ResolvedMethod)resolvedNode;
        JavaParser.ResolvedClass resolvedClass = resolvedMethod.getContainingClass();
        // The containing class must be subclass of android.app.Service
        if (!resolvedClass.isSubclassOf("android.app.Service", false)) {
            return super.visitMethodDeclaration(node);
        }

        // The return type must be "int"
        if (!node.astReturnTypeReference().isInt()) {
            return super.visitMethodDeclaration(node);
        }

        // The parameter types must match
        StrictListAccessor<VariableDefinition, MethodDeclaration> params = node.astParameters();
        if (null == params || 3 != params.size()) {
            return super.visitMethodDeclaration(node);
        }
        Iterator<VariableDefinition> iterable = params.iterator();
        VariableDefinition variable;
        int i = 0;
        while (iterable.hasNext()) {
            variable = iterable.next();
            if (!ONSTARTCOMMAND_PARAM_TYPES[i++].equals(mContext.getType(variable).getName())) {
                return super.visitMethodDeclaration(node);
            }
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
