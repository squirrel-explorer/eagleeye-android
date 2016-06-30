package com.squirrel_explorer.eagleeye.types.base;

import com.android.annotations.NonNull;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.JavaContext;

import java.lang.reflect.Constructor;

import lombok.ast.AstVisitor;

/**
 * Created by squirrel-explorer on 16/06/29.
 *
 * Base detector for java source, to be customized for more complex parsing
 */
public abstract class BaseJavaDetector extends Detector
        implements Detector.JavaScanner {
    protected Class mAstVisitorClazz;
    protected BaseAstVisitor mAstVisitor;

    public BaseJavaDetector(Class astVisitorClazz) {
        mAstVisitorClazz = astVisitorClazz;
    }

    @Override
    public AstVisitor createJavaVisitor(@NonNull JavaContext context) {
        if (null == mAstVisitor) {
            if (null != mAstVisitorClazz &&
                    BaseAstVisitor.class.isAssignableFrom(mAstVisitorClazz)) {
                try {
                    Constructor constructor = mAstVisitorClazz.getConstructor(JavaContext.class);
                    if (null != constructor) {
                        mAstVisitor = (BaseAstVisitor)constructor.newInstance(context);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mAstVisitor = null;
                }
            }
        }
        return mAstVisitor;
    }
}
