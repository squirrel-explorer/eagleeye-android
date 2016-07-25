package com.squirrel_explorer.eagleeye.utils;

import com.android.tools.lint.client.api.JavaParser;

/**
 * Created by squirrel-explorer on 16/7/25.
 */
public class CompatibilityUtils {
    /**
     * class  : com.android.tools.lint.client.api.JavaParser.ResolvedClass
     * method : public boolean isInPackage(String pkg, boolean includeSubPackages)
     * API start from : com.android.tools.lint:lint-api:24.5.0
     *
     * @param clazz
     * @param pkg
     * @param includeSubPackages
     * @return
     */
    public static boolean classInPackage(
            JavaParser.ResolvedClass clazz,
            String pkg, boolean includeSubPackages) {
        if (null == clazz ||
                null == pkg || pkg.isEmpty()) {
            return false;
        }

        String packageName = "";//clazz.getPackageName();

        if (pkg.equals(packageName)) {
            return true;
        }

        return includeSubPackages &&
                packageName.length() > pkg.length() &&
                '.' == packageName.charAt(pkg.length()) &&
                packageName.startsWith(pkg);
    }
}
