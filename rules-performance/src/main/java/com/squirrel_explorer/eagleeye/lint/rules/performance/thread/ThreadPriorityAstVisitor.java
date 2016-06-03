package com.squirrel_explorer.eagleeye.lint.rules.performance.thread;

import com.android.tools.lint.client.api.JavaParser;
import com.android.tools.lint.detector.api.JavaContext;
import com.eagleeye.types.base.BaseAstVisitor;
import com.eagleeye.utils.NodeUtils;

import java.util.ArrayList;

import lombok.ast.ConstructorInvocation;
import lombok.ast.Node;

/**
 * Created by squirrel-explorer on 16/02/22.
 */
public class ThreadPriorityAstVisitor extends BaseAstVisitor {
    public ThreadPriorityAstVisitor(JavaContext context) {
        super(context);
    }

    @Override
    public boolean visitConstructorInvocation(ConstructorInvocation node) {
        JavaParser.ResolvedNode resolved = mContext.resolve(node);
        if (null != resolved && resolved instanceof JavaParser.ResolvedMethod) {
            JavaParser.ResolvedMethod method = (JavaParser.ResolvedMethod)resolved;
            JavaParser.ResolvedClass clazz = method.getContainingClass();
            // Catch the thread created by new Thread()
            if (clazz.isSubclassOf("java.lang.Thread", false)) {
                // Get the enclosing method, since Thread.setPriority() may not be in the same level with new Thread()
                Node enclosingMethod = findEnclosingMethod(node);
                if (null != enclosingMethod) {
                    // Get thread variable name
                    Node enclosingVariable = findEnclosingVariable(node);
                    // Search all possible Thread.setPriority() in the method
                    ArrayList<Node> resultNodes = new ArrayList<Node>();
                    findSetPriorityNodes(enclosingMethod, resultNodes);
                    // Find the last Thread.setPriority() in the above list.
                    // If an upper priority is set, prompt warning. However, if no priority
                    // is set, it means that the thread priority is the same as the current
                    // thread (maybe UI thread), so prompt warning, too.
                    Node setPriorityNode = determineSetPriorityNode(enclosingVariable, resultNodes);
                    if (null == setPriorityNode) {
                        mContext.report(
                                ThreadPriorityDetector.ISSUE,
                                mContext.getLocation(node),
                                "Please set lower priority for new thread");
                    } else {
                        String threadPriority = null;
                        for (Node child : setPriorityNode.getChildren()) {
                            String childValue = child.toString();
                            if (childValue.contains("THREAD_PRIORITY_")) {
                                threadPriority = childValue;
                                break;
                            }
                        }
                        if (threadPriority.contains("THREAD_PRIORITY_FOREGROUND") ||
                                threadPriority.contains("THREAD_PRIORITY_DISPLAY") ||
                                threadPriority.contains("THREAD_PRIORITY_URGENT_DISPLAY") ||
                                threadPriority.contains("THREAD_PRIORITY_AUDIO") ||
                                threadPriority.contains("THREAD_PRIORITY_URGENT_AUDIO")) {
                            mContext.report(
                                    ThreadPriorityDetector.ISSUE,
                                    mContext.getLocation(setPriorityNode),
                                    "Please set lower priority for new thread");
                        }
                    }
                    resultNodes.clear();
                }
            }
        }

        return super.visitConstructorInvocation(node);
    }

    /**
     * Get the variable node which is assigned by new Thread()
     *
     * @param node  new Thread() node
     * @return
     */
    private Node findEnclosingVariable(Node node) {
        for (Node child : node.getParent().getChildren()) {
            if (!child.equals(node)) {
                return child;
            }
        }
        return null;
    }

    /**
     * Get the enclosing method node of new Thread() node
     *
     * @param node  new Thread() node
     * @return
     */
    private Node findEnclosingMethod(Node node) {
        JavaParser.ResolvedMethod method = NodeUtils.findEnclosingMethod(mContext, node);
        if (null == method) {
            return null;
        }

        Node parentNode = node;
        JavaParser.ResolvedNode parentResolved;
        do {
            parentNode = parentNode.getParent();
            parentResolved = mContext.resolve(parentNode);
        } while (!(method.equals(parentResolved)));

        return parentNode;
    }

    /**
     * Search all possible Thread.setPriority() nodes
     *
     * @param node
     * @param resultNodes
     */
    private void findSetPriorityNodes(Node node, ArrayList<Node> resultNodes) {
        if ("setPriority".equals(node.toString())) {
            resultNodes.add(node);
        }

        for (Node child : node.getChildren()) {
            findSetPriorityNodes(child, resultNodes);
        }
    }

    /**
     * Check if there exists Thread.setPriority() node. If there are multiple nodes,
     * find the last one.
     *
     * @param var           thread node assigned by new Thread()
     * @param resultNodes   the list of possible Thread.setPriority() nodes
     * @return
     */
    private Node determineSetPriorityNode(Node var, ArrayList<Node> resultNodes) {
        if (null == resultNodes || resultNodes.isEmpty()) {
            return null;
        }

        String varName = var.toString();
        Node setPriorityNode = null;
        for (Node result : resultNodes) {
            boolean found = false;
            Node parent = result.getParent();
            for (Node child : parent.getChildren()) {
                String childValue = child.toString();
                if (varName.equals(childValue)) {
                    found = true;
                }
                if (found && childValue.contains("THREAD_PRIORITY_")) {
                    break;
                }
            }
            if (found) {
                setPriorityNode = parent;
            }
        }

        return setPriorityNode;
    }
}
