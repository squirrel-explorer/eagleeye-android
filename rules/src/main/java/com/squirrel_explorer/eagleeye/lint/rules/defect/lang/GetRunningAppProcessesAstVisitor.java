package com.squirrel_explorer.eagleeye.lint.rules.defect.lang;

import com.android.tools.lint.client.api.JavaParser;
import com.android.tools.lint.detector.api.JavaContext;
import com.squirrel_explorer.eagleeye.lint.types.base.BaseAstVisitor;
import com.squirrel_explorer.eagleeye.lint.utils.NodeUtils;

import lombok.ast.MethodInvocation;

public class GetRunningAppProcessesAstVisitor extends BaseAstVisitor {
    // Signature of ActivityManager.getRunningAppProcesses()
    private static final String getRunningAppProcesses_SIGNATURE = "public List<android.app.ActivityManager.RunningAppProcessInfo> getRunningAppProcesses() ";

    public GetRunningAppProcessesAstVisitor(JavaContext context) {
        super(context);
    }

    @Override
    public boolean visitMethodInvocation(MethodInvocation node) {
        JavaParser.ResolvedMethod resolvedMethod = NodeUtils.parseResolvedMethod(mContext, node);
        if (null == resolvedMethod) {
            return super.visitMethodInvocation(node);
        }

        // The containing class must be subclass of android.app.ActivityManager
        JavaParser.ResolvedClass resolvedClass = resolvedMethod.getContainingClass();
        if (!resolvedClass.isSubclassOf("android.app.ActivityManager", false)) {
            return super.visitMethodInvocation(node);
        }

        // Only handle ActivityManager.getRunningAppProcesses()
        if (!getRunningAppProcesses_SIGNATURE.equals(resolvedMethod.getSignature())) {
            return super.visitMethodInvocation(node);
        }

        mContext.report(
                GetRunningAppProcessesDetector.ISSUE,
                mContext.getLocation(node),
                "Please DO NOT invoke ActivityManager.getRunningAppProcesses(), since it may cause performance issue and not applicable in Android 5.1+ .");

        return super.visitMethodInvocation(node);
    }
}
