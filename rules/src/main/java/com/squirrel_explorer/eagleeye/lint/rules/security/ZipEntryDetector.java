package com.squirrel_explorer.eagleeye.lint.rules.security;

import com.android.annotations.NonNull;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

import lombok.ast.AstVisitor;

/**
 * Created by 宝宝 on 2017/3/13.
 * Function: Scan risks which may cause security problem
 * 本规则功能:检查代码中可能引起ConcurrentModificationException的写法
 * 这类问题的详细解释：http://www.2cto.com/article/201606/519825.html
 */
public class ZipEntryDetector extends Detector implements Detector.JavaScanner{

     public static final Issue ISSUE = Issue.create(
               "ZipEntryDetector",
               "be attention to use zipEntry.getName() ,if return string contains ../,it will cause security risks",
               "在解压缩操作时候使用了zipEntry.getName()函数，但是没有过滤getName()函数返回值是否含有字符串,请检查zipEntry.getName()的返回值中是否含有字符串../，如果有，则是有危险的Zip包（可抛异常等操作），如果没有，则正常执行。",
               Category.SECURITY,
               5,
               Severity.FATAL,
               new Implementation(
                         ZipEntryDetector.class,
                         Scope.JAVA_FILE_SCOPE));

     @Override
     public AstVisitor createJavaVisitor(@NonNull JavaContext context) {
          return new ZipEntryAstVisitor(context);
     }
}
