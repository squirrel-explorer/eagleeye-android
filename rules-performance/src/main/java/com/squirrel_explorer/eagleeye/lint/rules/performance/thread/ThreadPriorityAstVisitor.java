package com.squirrel_explorer.eagleeye.lint.rules.performance.thread;

import com.android.tools.lint.client.api.JavaParser;
import com.android.tools.lint.detector.api.JavaContext;
import com.squirrel_explorer.eagleeye.types.base.BaseAstVisitor;
import com.squirrel_explorer.eagleeye.utils.NodeUtils;

import java.util.ArrayList;
import java.util.List;

import lombok.ast.ConstructorInvocation;
import lombok.ast.Expression;
import lombok.ast.MethodInvocation;
import lombok.ast.Node;
import lombok.ast.StrictListAccessor;

/**
 * Created by squirrel-explorer on 16/02/22.
 */
public class ThreadPriorityAstVisitor extends BaseAstVisitor {
    public ThreadPriorityAstVisitor(JavaContext context) {
        super(context);
    }

    @Override
    public boolean visitConstructorInvocation(ConstructorInvocation node) {
        // 寻找new Thread()的节点
        JavaParser.ResolvedClass typeClass = NodeUtils.parseContainingClass(mContext, node);
        if (null == typeClass || !typeClass.isSubclassOf("java.lang.Thread", false)) {
            return super.visitConstructorInvocation(node);
        }

        // 获取new Thread()的赋值对象
        Node operand = findAssignOperand(node);
        // 获取new Thread()所在方法
        Node containingMethod = JavaContext.findSurroundingMethod(node);
        if (null == containingMethod) {
            return super.visitConstructorInvocation(node);
        }

        // 获取containingMethod中所有的operand.setPriority()节点的列表
        ArrayList<MethodInvocation> setPriorityNodes = new ArrayList<>();
        searchSetPriorityNodes(operand, containingMethod, setPriorityNodes);
        // 找出最后一个operand.setPriority()节点
        MethodInvocation lastSetPriorityNode = setPriorityNodes.isEmpty() ? null : setPriorityNodes.get(setPriorityNodes.size() - 1);
        if (null == lastSetPriorityNode) {
            // 在Android里，new Thread()创建的线程，缺省与当前线程具有相同优先级，所以
            // 如果没有显式降低优先级，我们认为是有问题的情形
            mContext.report(
                    ThreadPriorityDetector.ISSUE,
                    mContext.getLocation(node),
                    "Please set lower priority for new thread");
        } else {
            // 如果最后一次调用operand.setPriority()没有设置较低优先级，则报警
            if (!validateSetLowerPriority(lastSetPriorityNode)) {
                mContext.report(
                        ThreadPriorityDetector.ISSUE,
                        mContext.getLocation(lastSetPriorityNode),
                        "Please set lower priority for new thread");
            }
        }
        setPriorityNodes.clear();

        return super.visitConstructorInvocation(node);
    }

    /**
     * 获取new Thread()的赋值对象节点
     *
     * @param node  new Thread()构造函数节点
     * @return  Thread t = new Thread()的赋值对象节点t，对于new Thread().xxx()形式，
     *          赋值对象节点为new Thread()本身
     */
    private Node findAssignOperand(Node node) {
        Node parentNode = node.getParent();
        if (null == parentNode) {
            return node;
        }
        return parentNode.getChildren().get(0);
    }

    /**
     * 给定thread变量名，获取其所有的setPriority()节点列表
     *
     * @param operand
     * @param node
     * @param setPriorityNodes
     */
    private void searchSetPriorityNodes(Node operand, Node node, ArrayList<MethodInvocation> setPriorityNodes) {
        JavaParser.ResolvedMethod resolvedMethod = NodeUtils.parseResolvedMethod(mContext, node);
        if (null != resolvedMethod &&
                "setPriority".equals(resolvedMethod.getName()) &&
                resolvedMethod.getContainingClass().isSubclassOf("java.lang.Thread", false) &&
                node instanceof MethodInvocation) {
            MethodInvocation methodInvocation = (MethodInvocation)node;
            // 这里比较的是Node.toString()而非Node本身，原因在于，赋值节点的operand是Identifier，
            // 而此处Thread.setPriority()节点的operand确实VariableReference。虽然它们的字符串值
            // 相同，但是在AST里却是不同类型的节点。
            if (methodInvocation.rawOperand().toString().equals(operand.toString())) {
                setPriorityNodes.add(methodInvocation);
            }
        }

        List<Node> children = node.getChildren();
        if (null != children && !children.isEmpty()) {
            for (Node child : node.getChildren()) {
                searchSetPriorityNodes(operand, child, setPriorityNodes);
            }
        }
    }

    /**
     * 检查Thread.setPriority()节点是否设置了较低优先级
     *
     * @param node  Thread.setPriority()节点
     * @return  true: 较低优先级
     *          false: 不低于当前线程优先级
     */
    private boolean validateSetLowerPriority(MethodInvocation node) {
        StrictListAccessor<Expression, MethodInvocation> args = node.astArguments();
        if (null == args || 1 != args.size()) {
            throw new IllegalArgumentException("The number of arguments is mismatched for Thread.setPriority().");
        }

        // 因为setPriority()的参数未必是常量，所以在纯语法分析中直接比较是很困难的，这里为
        // 简化问题，只对设置较高优先级的情形返回false
        String priority = args.first().toString();
        if (priority.contains("NORM_PRIORITY") ||                       // Thread.NORM_PRIORITY
                priority.contains("MAX_PRIORITY") ||                    // Thread.MAX_PRIORITY
                priority.contains("THREAD_PRIORITY_DEFAULT") ||         // Process.THREAD_PRIORITY_DEFAULT
                priority.contains("THREAD_PRIORITY_MORE_FAVORABLE") ||  // Process.THREAD_PRIORITY_MORE_FAVORABLE
                priority.contains("THREAD_PRIORITY_FOREGROUND") ||      // Process.THREAD_PRIORITY_FOREGROUND
                priority.contains("THREAD_PRIORITY_DISPLAY") ||         // Process.THREAD_PRIORITY_DISPLAY
                priority.contains("THREAD_PRIORITY_URGENT_DISPLAY") ||  // Process.THREAD_PRIORITY_URGENT_DISPLAY
                priority.contains("THREAD_PRIORITY_AUDIO") ||           // Process.THREAD_PRIORITY_AUDIO
                priority.contains("THREAD_PRIORITY_URGENT_AUDIO")) {    // Process.THREAD_PRIORITY_URGENT_AUDIO
            return false;
        }
        return true;
    }
}
