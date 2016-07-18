package com.squirrel_explorer.eagleeye.utils;

import com.android.annotations.NonNull;
import com.android.tools.lint.client.api.JavaParser;
import com.android.tools.lint.detector.api.JavaContext;

import lombok.ast.ClassDeclaration;
import lombok.ast.Node;

/**
 * Created by squirrel-explorer on 16/02/22.
 */
public class NodeUtils {
    public static JavaParser.ResolvedMethod parseResolvedMethod(
            @NonNull JavaContext context, @NonNull Node node) {
        if (null == context || null == node) {
            return null;
        }

        JavaParser.ResolvedNode resolvedNode = null;
        try {
            resolvedNode = context.resolve(node);
        } catch (Exception e) {
            // TODO
        }

        return (resolvedNode instanceof JavaParser.ResolvedMethod) ?
                (JavaParser.ResolvedMethod)resolvedNode : null;
    }

    public static JavaParser.ResolvedClass parseContainingClass(
            @NonNull JavaContext context, @NonNull Node node) {
        if (null == context || null == node) {
            return null;
        }

        JavaParser.ResolvedNode resolvedNode = null;
        try {
            resolvedNode = context.resolve(node);
        } catch (Exception e) {
            // TODO
        }

        if (resolvedNode instanceof JavaParser.ResolvedMethod) {
            return ((JavaParser.ResolvedMethod)resolvedNode).getContainingClass();
        } else if (resolvedNode instanceof JavaParser.ResolvedField) {
            return ((JavaParser.ResolvedField)resolvedNode).getContainingClass();
        } else if (resolvedNode instanceof JavaParser.ResolvedClass) {
            return ((JavaParser.ResolvedClass)resolvedNode).getContainingClass();
        } else {
            ClassDeclaration classDeclaration = JavaContext.findSurroundingClass(node);
            if (null == classDeclaration) {
                return null;
            }
            resolvedNode = context.resolve(classDeclaration);
            return (resolvedNode instanceof JavaParser.ResolvedClass) ?
                    (JavaParser.ResolvedClass)resolvedNode : null;
        }
    }
}
