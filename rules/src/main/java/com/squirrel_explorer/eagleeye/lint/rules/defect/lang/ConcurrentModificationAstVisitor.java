package com.squirrel_explorer.eagleeye.lint.rules.defect.lang;

import com.android.tools.lint.client.api.JavaParser;
import com.android.tools.lint.detector.api.JavaContext;
import com.squirrel_explorer.eagleeye.lint.types.base.BaseAstVisitor;
import com.squirrel_explorer.eagleeye.lint.utils.NodeUtils;

import java.util.Arrays;
import java.util.List;

import lombok.ast.DoWhile;
import lombok.ast.For;
import lombok.ast.ForEach;
import lombok.ast.KeywordModifier;
import lombok.ast.MethodDeclaration;
import lombok.ast.MethodInvocation;
import lombok.ast.Modifiers;
import lombok.ast.Node;
import lombok.ast.Synchronized;
import lombok.ast.While;

/**
 * Created by squirrel-explorer on 16/3/27.
 *
 * ConcurrentModificationException的详细说明见:
 * https://developer.android.com/reference/java/util/ConcurrentModificationException.html
 * 这个问题在单线程和多线程环境里都有可能发生,本质原因是Iterator在遍历Collection的过程中,
 * modCount和expectedModCount不同步。所以本规则对常见的有隐患的情形进行了检测,但需要说明
 * 的是,这些并不是全部,因为任何modCount和expectedModCount不同步的场景都有可能发生ConcurrentModificationException。
 *
 * 单线程:
 * ArrayList<String> list = new ArrayList<>();
 * list.add("1");
 * list.add("2");
 * list.add("3");
 * for (String str : list) {
 *     ......
 *     list.remove(str);
 * }
 *
 * list的foreach循环,隐式调用了list.iterator(),这样list.remove()iterator.next()
 * 将会冲突。
 *
 * 多线程:
 * ArrayList<String> list = new ArrayList<>();
 * list.add("1");
 * list.add("2");
 * list.add("3");
 * Iterator iterator = list.iterator();
 * synchronized(sync) {
 *     while(iterator.hasNext()) {
 *         String str = iterator.next();
 *         ......
 *         iterator.remove();
 *     }
 * }
 *
 * while循环里的内容,如果在多线程环境里不做同步的保护,则可能出现不同线程里的iterator,
 * modCount和expectedModCount各不相同。
 */
public class ConcurrentModificationAstVisitor extends BaseAstVisitor {
    public ConcurrentModificationAstVisitor(JavaContext context) {
        super(context);
    }

    @Override
    public boolean visitMethodInvocation(MethodInvocation node) {
        JavaParser.ResolvedMethod resolvedMethod = NodeUtils.parseResolvedMethod(mContext, node);
        if (null == resolvedMethod) {
            return super.visitMethodInvocation(node);
        }

        checkSingleThreadIterating(node);
        checkMultiThreadSync(node);

        return super.visitMethodInvocation(node);
    }

    private static List<String> MODCOUNT_CHANGEABLE_METHODS_SIGNATURE = Arrays.asList(
            "public boolean add(java.lang.Object) ",
            "public boolean addAll(Collection<?>) ",
            "public boolean remove(java.lang.Object) ",
            "public boolean removeAll(Collection<?>) ",
            "public boolean retainAll(Collection<?>) ",
            "public void clear() "
    );

    private boolean modCountChangeable(MethodInvocation node) {
        JavaParser.ResolvedMethod resolvedMethod = NodeUtils.parseResolvedMethod(mContext, node);
        if (null == resolvedMethod) {
            return false;
        }

        // 待判断的Method限定为java.util.Collection的部分可能改变modCount的方法
        return (resolvedMethod.getContainingClass().isImplementing("java.util.Collection", false) &&
                MODCOUNT_CHANGEABLE_METHODS_SIGNATURE.contains(resolvedMethod.getSignature()));
    }

    private boolean checkSingleThreadIterating(MethodInvocation node) {
        boolean ret = false;

        if (!modCountChangeable(node)) {
            return ret;
        }

        // 获取Method的operand,此处应为一个Collection实例
        String operand = node.astOperand().toString();

        // 获取当前调用所在的方法定义,作为检查的边界
        Node surroundingMethod = JavaContext.findSurroundingMethod(node);
        Node parent = node.getParent();
        // 试图寻找,可能改变operand modCount的方法,是否运行在operand的iterating中
        while (surroundingMethod != parent) {
            if (parent instanceof ForEach &&
                    operand.equals(((ForEach)parent).astIterable().toString())) {
                ret = true;
                mContext.report(
                        ConcurrentModificationDetector.ISSUE,
                        mContext.getLocation(node),
                        "Please DO NOT modify elements DIRECTLY when iterating \"" + operand + "\".");
                break;
            }
            parent = parent.getParent();
        }

        return ret;
    }

    private static List<String> ITERATOR_METHODS_SIGNATURE = Arrays.asList(
            "public abstract void remove() ",
            "public void remove() "
    );

    private boolean isIteratorMethod(MethodInvocation node) {
        JavaParser.ResolvedMethod resolvedMethod = NodeUtils.parseResolvedMethod(mContext, node);
        if (null == resolvedMethod) {
            return false;
        }

        // 待判断的Method限定为java.util.Collection的部分可能改变modCount的方法
        return (resolvedMethod.getContainingClass().isImplementing("java.util.Iterator", false) &&
                ITERATOR_METHODS_SIGNATURE.contains(resolvedMethod.getSignature()));
    }

    private boolean checkMultiThreadSync(MethodInvocation node) {
        boolean ret = false;

        // 判断是否为可能修改Iterator的方法
        if (!isIteratorMethod(node)) {
            return ret;
        }

        // 获取Method的operand,此处应为一个Iterator实例
        String operand = node.astOperand().toString();

        // 获取当前调用所在的方法定义,作为检查的边界
        Node surroundingNode = JavaContext.findSurroundingMethod(node);
        MethodDeclaration surroundingMethod;
        if (surroundingNode instanceof MethodDeclaration) {
            surroundingMethod = (MethodDeclaration)surroundingNode;
        } else {
            return ret;
        }
        Node parent = node.getParent();
        // 试图寻找可能改变发生Iterator iterating的循环
        while (surroundingMethod != parent) {
            // 因为Iterator遍历的写法可能有很多种,此处为了提高扫描的精确性,做了一个
            // 加强的判断,就是检测是否确实有Iterator.next()的存在
            if (parent instanceof While ||
                    parent instanceof DoWhile ||
                    parent instanceof For ||
                    parent instanceof ForEach) {
                if (null != NodeUtils.findNodeByLiterals(parent,
                        MethodInvocation.class,
                        operand + ".next()")) {
                    break;
                }
            }
            parent = parent.getParent();
        }

        if (surroundingMethod != parent) {      // 已找到Iterator遍历的循环
            // 判断是否有synchronized同步控制
            boolean foundSync = false;
            parent = parent.getParent();
            while (surroundingMethod != parent) {
                if (parent instanceof Synchronized) {
                    foundSync = true;
                    break;
                }
                parent = parent.getParent();
            }

            // 如果没有synchronized块,则再判断是否最外面方法定义的Modifier里有synchronized修饰
            if (!foundSync) {
                Modifiers modifiers = surroundingMethod.astModifiers();
                if (null != modifiers && null != modifiers.astKeywords()) {
                    for (KeywordModifier modifier : modifiers.astKeywords()) {
                        if ("synchronized".equals(modifier.astName())) {
                            foundSync = true;
                            break;
                        }
                    }
                }
            }

            // 确认没有synchronized保护,报警
            if (!foundSync) {
                ret = true;
                mContext.report(
                        ConcurrentModificationDetector.ISSUE,
                        mContext.getLocation(node),
                        "Please BE CAREFUL OF modifying \"" + operand + "\" when iterating, without synchronization protection. It may cause ConcurrentModificationException in multi-thread environments.");
            }
        }

        return ret;
    }
}
