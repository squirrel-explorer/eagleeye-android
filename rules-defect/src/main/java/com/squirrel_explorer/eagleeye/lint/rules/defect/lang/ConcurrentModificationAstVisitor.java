package com.squirrel_explorer.eagleeye.lint.rules.defect.lang;

import com.android.tools.lint.client.api.JavaParser;
import com.android.tools.lint.detector.api.JavaContext;
import com.squirrel_explorer.eagleeye.types.base.BaseAstVisitor;

import lombok.ast.DoWhile;
import lombok.ast.For;
import lombok.ast.ForEach;
import lombok.ast.MethodInvocation;
import lombok.ast.Node;
import lombok.ast.While;

/**
 * Created by squirrel-explorer on 16/3/27.
 */
public class ConcurrentModificationAstVisitor extends BaseAstVisitor {
    public ConcurrentModificationAstVisitor(JavaContext context) {
        super(context);
    }

    @Override
    public boolean visitMethodInvocation(MethodInvocation node) {
        // Check invocation of "add" or "remove"
        String methodName = node.astName().astValue();
        if (!"add".equals(methodName) &&
                !"remove".equals(methodName)) {
            return super.visitMethodInvocation(node);
        }

        // Find the operand of "add" or "remove"
        Node opNode = node.rawOperand();
        if (null == opNode) {
            return super.visitMethodInvocation(node);
        }

        // Check if the class of operand is implementing java.util.Collection
        JavaParser.TypeDescriptor opNodeType = mContext.getType(opNode);
        if (null == opNodeType) {
            return super.visitMethodInvocation(node);
        }
        JavaParser.ResolvedClass opClass = opNodeType.getTypeClass();
        boolean isCollection = false;
        if (null != opClass && opClass.isImplementing("java.util.Collection", false)) {
            isCollection = true;
        }
        if (!isCollection) {
            return super.visitMethodInvocation(node);
        }

        // Find the enclosing method
        Node enclosingMethod = JavaContext.findSurroundingMethod(opNode);
        if (null == enclosingMethod) {
            return super.visitMethodInvocation(node);
        }

        // Check if the invocation of "add" or "remove" exists in some loop
        Node parentNode = opNode.getParent();
        boolean enclosingLoop = false;
        while (null != parentNode &&
                enclosingMethod != parentNode) {
            if (parentNode instanceof ForEach) {
                if (opNode.toString().equals(((ForEach)parentNode).astIterable().toString())) {
                    enclosingLoop = true;
                }
            } else if (parentNode instanceof For ||
                    parentNode instanceof DoWhile ||
                    parentNode instanceof While) {
                enclosingLoop = true;
            }

            if (enclosingLoop) {
                mContext.report(
                        ConcurrentModificationDetector.ISSUE,
                        mContext.getLocation(node),
                        "Please DO NOT modify elements DIRECTLY when loop-traversing " + opClass.getName());
                break;
            }

            parentNode = parentNode.getParent();
        }

        return super.visitMethodInvocation(node);
    }
}
