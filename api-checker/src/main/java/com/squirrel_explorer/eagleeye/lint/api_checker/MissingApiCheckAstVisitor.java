package com.squirrel_explorer.eagleeye.lint.api_checker;

import com.android.tools.lint.client.api.JavaParser;
import com.android.tools.lint.detector.api.JavaContext;
import com.squirrel_explorer.eagleeye.types.base.BaseAstVisitor;
import com.squirrel_explorer.eagleeye.utils.CompatibilityUtils;
import com.squirrel_explorer.eagleeye.utils.NodeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import lombok.ast.Comment;
import lombok.ast.MethodDeclaration;
import lombok.ast.MethodInvocation;
import lombok.ast.Modifiers;

/**
 * Created by squirrel-explorer on 2016-06-03.
 */
public class MissingApiCheckAstVisitor extends BaseAstVisitor {
    private int mMinSdkVersion, mMaxSdkVersion;
    private MissingApiDatabase mMissingApiDb;

    public MissingApiCheckAstVisitor(JavaContext context) {
        super(context);

        if (1 == MissingApiChecker.MODE) {
            mMinSdkVersion = context.getProject().getMinSdk();
            mMaxSdkVersion = context.getProject().getClient().getHighestKnownApiLevel();
        }
    }

    public void setMissingApiDb(MissingApiDatabase missingApiDb) {
        if (1 == MissingApiChecker.MODE) {
            mMissingApiDb = missingApiDb;
        }
    }

    @Override
    public boolean visitMethodInvocation(MethodInvocation node) {
        if (1 == MissingApiChecker.MODE) {
            JavaParser.ResolvedMethod resolvedMethod = NodeUtils.parseResolvedMethod(mContext, node);
            if (null == resolvedMethod) {
                return super.visitMethodInvocation(node);
            }

            String clazzName = resolvedMethod.getContainingClass().getName();
            String methodSignature = resolvedMethod.getSignature();
            boolean isMissing = validateRemoved(node, clazzName, methodSignature);
            if (!isMissing) {
                isMissing = validateHide(node, clazzName, methodSignature);
            }
        }

        return super.visitMethodInvocation(node);
    }

    private boolean validateRemoved(MethodInvocation node, String clazzName, String methodSignature) {
        for (int apiLevel = mMinSdkVersion; apiLevel <= mMaxSdkVersion; apiLevel++) {
            if (mMissingApiDb.isRemoved(apiLevel, clazzName, methodSignature)) {
                mContext.report(MissingApiChecker.ISSUE,
                        mContext.getLocation(node),
                        "The method of \"" + node.astName() + "\" has been REMOVED in Android SDK " + apiLevel);
                return true;
            }
        }
        return false;
    }

    private boolean validateHide(MethodInvocation node, String clazzName, String methodSignature) {
        for (int apiLevel = mMinSdkVersion; apiLevel <= mMaxSdkVersion; apiLevel++) {
            if (mMissingApiDb.isHide(apiLevel, clazzName, methodSignature)) {
                mContext.report(MissingApiChecker.ISSUE,
                        mContext.getLocation(node),
                        "The method of \"" + node.astName() + "\" has been HIDDEN in Android SDK " + apiLevel);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean visitMethodDeclaration(MethodDeclaration node) {
        boolean ret = true;

        try {
            if (0 == MissingApiChecker.MODE) {
                printMissingMethod(node);
            }

            ret = super.visitMethodDeclaration(node);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    public static HashMap<String, ArrayList<String>> removedMethods = new HashMap<>();
    public static HashMap<String, ArrayList<String>> hideMethods = new HashMap<>();

    private void printMissingMethod(MethodDeclaration node) {
        // Ignore constructor
        if (null == node.astReturnTypeReference()) {
            return;
        }

        // Only public/protected methods
        Modifiers modifiers = node.astModifiers();
        if (null == modifiers ||
                (!modifiers.isPublic() && !modifiers.isProtected())) {
            return;
        }

        JavaParser.ResolvedMethod resolvedMethod = NodeUtils.parseResolvedMethod(mContext, node);
        if (null == resolvedMethod) {
            return;
        }

        JavaParser.ResolvedClass resolvedClass = resolvedMethod.getContainingClass();
        if (null == resolvedClass || !isInAndroidSdk(resolvedClass)) {
            return;
        }

        if (isRemoved(node)) {
            appendMethod(removedMethods, resolvedClass.getName(), resolvedMethod.getSignature());
        }
        if (isHide(node)) {
            appendMethod(hideMethods, resolvedClass.getName(), resolvedMethod.getSignature());
        }
    }

    private void appendMethod(HashMap<String, ArrayList<String>> methodSet, String clazz, String method) {
        ArrayList<String> methods;
        if (methodSet.containsKey(clazz)) {
            methods = methodSet.get(clazz);
        } else {
            methods = new ArrayList<>();
            methodSet.put(clazz, methods);
        }
        methods.add(method);
    }

    private static final String[] ANDROID_PACKAGES = {
            "android",          // android.*
            "com.android",      // com.android.*
            "java",             // java.*
            "javax",            // javax.*
    };

    private boolean isInAndroidSdk(JavaParser.ResolvedClass clazz) {
        for (String androidPackage : ANDROID_PACKAGES) {
            if (CompatibilityUtils.classInPackage(clazz, androidPackage, true)) {
                return true;
            }
        }
        return false;
    }

    private static final Pattern REMOVED_DETECTOR = Pattern.compile("^(?:.*(?:[*{}]|\\s))?@removed(?:(?:[*{}]|\\s).*)?$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern HIDE_DETECTOR = Pattern.compile("^(?:.*(?:[*{}]|\\s))?@hide(?:(?:[*{}]|\\s).*)?$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private boolean isRemoved(MethodDeclaration node) {
        Comment comment = node.astJavadoc();
        return (null == comment ? false : REMOVED_DETECTOR.matcher(comment.astContent()).matches());
    }

    private boolean isHide(MethodDeclaration node) {
        Comment comment = node.astJavadoc();
        return (null == comment ? false : HIDE_DETECTOR.matcher(comment.astContent()).matches());
    }
}
