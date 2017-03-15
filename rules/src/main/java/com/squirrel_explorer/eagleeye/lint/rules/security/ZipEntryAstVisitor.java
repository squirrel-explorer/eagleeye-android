package com.squirrel_explorer.eagleeye.lint.rules.security;

import com.android.tools.lint.client.api.JavaParser;
import com.android.tools.lint.detector.api.JavaContext;
import com.squirrel_explorer.eagleeye.lint.types.base.BaseAstVisitor;
import com.squirrel_explorer.eagleeye.lint.utils.NodeUtils;

import lombok.ast.MethodInvocation;
import lombok.ast.Node;

/**
 * Created by 宝宝 on 2017/3/13.
 *TODO 这条规则写的不是很好，但是现在AST只能做到在当前构造函数里面去检查，只有CFG支持后才能检查出其他构造函数中的调用
 */
public class ZipEntryAstVisitor extends BaseAstVisitor {

     public ZipEntryAstVisitor(JavaContext context) {
          super(context);
     }

     @Override
     public boolean visitMethodInvocation(MethodInvocation node) {
          JavaParser.ResolvedMethod resolvedMethod = NodeUtils.parseResolvedMethod(mContext, node);
          if (null == resolvedMethod) {
               return  super.visitMethodInvocation(node);
          }
          //判断是不是zipEntry.getName()函数
          if ("java.util.zip.ZipEntry".equals(resolvedMethod.getContainingClass().getName()) && "public java.lang.String getName() ".equals(resolvedMethod.getSignature())) {
               //找到当前调用所在的方法定义边界
               boolean isSafe = false;
               Node surroundingNode = JavaContext.findSurroundingMethod(node);
               Node parent = node.getParent();
               while (surroundingNode != parent) {
                    //判断有没有做保护
                    String judgeState = ".contains(\"../\")";
                    if (parent.toString().contains(judgeState)) {
                         isSafe = true;
                         break;
                    }
                    parent = parent.getParent();
               }
               if (!isSafe) {
                    mContext.report(
                              ZipEntryDetector.ISSUE,
                              mContext.getLocation(node),
                              "请检查zipEntry.getName()的返回值中是否含有字符串../，如果有，则是有危险的Zip包（可抛异常等操作"
                    );
               }
          }
          return super.visitMethodInvocation(node);
     }
}
