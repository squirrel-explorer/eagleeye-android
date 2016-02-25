package com.eagleeye.utils;

import com.android.tools.lint.client.api.JavaParser;
import com.android.tools.lint.detector.api.JavaContext;

import java.util.ArrayList;

import lombok.ast.Node;

/**
 * Created by squirrel-explorer on 16/02/22.
 */
public class NodeUtils {
    /**
     * Find the Class node of some node
     *
     * @param context
     * @param node
     * @return
     */
    public static JavaParser.ResolvedClass findEnclosingClass(JavaContext context, Node node) {
        if (null == context || null == node) {
            return null;
        }

        Node parentNode = node;
        JavaParser.ResolvedNode parentResolved;
        do {
            try {
                parentNode = parentNode.getParent();
                parentResolved = context.resolve(parentNode);
            } catch (Exception e) {
                parentResolved = null;
                break;
            }
        } while (!(parentResolved instanceof JavaParser.ResolvedMethod) &&
                !(parentResolved instanceof JavaParser.ResolvedField) &&
                !(parentResolved instanceof JavaParser.ResolvedClass));

        JavaParser.ResolvedClass containingClazz;
        if (parentResolved instanceof JavaParser.ResolvedMethod) {
            containingClazz = ((JavaParser.ResolvedMethod)parentResolved).getContainingClass();
        } else if (parentResolved instanceof JavaParser.ResolvedField) {
            containingClazz = ((JavaParser.ResolvedField)parentResolved).getContainingClass();
        } else if (parentResolved instanceof JavaParser.ResolvedClass) {
            containingClazz = (JavaParser.ResolvedClass)parentResolved;
        } else {
            containingClazz = null;
        }

        return containingClazz;
    }

    /**
     * Find the Method node of some node
     *
     * @param context
     * @param node
     * @return
     */
    public static JavaParser.ResolvedMethod findEnclosingMethod(JavaContext context, Node node) {
        if (null == context || null == node) {
            return null;
        }

        JavaParser.ResolvedClass containingClazz = findEnclosingClass(context, node);

        Node parentNode = node;
        JavaParser.ResolvedNode parentResolved;
        ArrayList<JavaParser.ResolvedNode> methods = new ArrayList<JavaParser.ResolvedNode>();
        do {
            try {
                parentNode = parentNode.getParent();
                parentResolved = context.resolve(parentNode);
                methods.add(parentResolved);
            } catch (Exception e) {
                break;
            }
        } while (containingClazz != parentResolved);

        for (int i = methods.size() - 1; i >= 0; i--) {
            if (methods.get(i) instanceof JavaParser.ResolvedMethod) {
                return (JavaParser.ResolvedMethod)methods.get(i);
            }
        }
        return null;
    }
}
