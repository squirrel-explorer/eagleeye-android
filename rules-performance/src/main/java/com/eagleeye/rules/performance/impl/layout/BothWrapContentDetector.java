package com.eagleeye.rules.performance.impl.layout;

import com.android.SdkConstants;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.LayoutDetector;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.android.tools.lint.detector.api.XmlContext;

import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by squirrel-explorer on 16/02/22.
 *
 * Function: Check layout xmls, and prompt warning for VIEWs with both layout_width
 * and layout_height WRAP_CONTENT. Currently, it is only applied on TextView.
 *
 * 本规则功能：检查layout资源文件，对于layout_width和layout_height都设为wrap_content的
 * View（暂时只控制在TextView），提示最好至少有一个属性为固定值或match_parent
 */
public class BothWrapContentDetector extends LayoutDetector {
    public static final Issue ISSUE = Issue.create(
            "BothWrapContentDetector",
            "Avoid using both wrap_content in width & height",
            "This app should use a fixed value at least in one of width & height.",
            Category.PERFORMANCE,
            2,
            Severity.WARNING,
            new Implementation(
                    BothWrapContentDetector.class,
                    Scope.RESOURCE_FILE_SCOPE));

    @Override
    public Collection<String> getApplicableElements() {
        return Arrays.asList(
                SdkConstants.TEXT_VIEW,
                "android.widget.TextView",
                SdkConstants.BUTTON,
                "android.widget.Button");
    }

    private static final String WRAP_CONTENT = "wrap_content";

    @Override
    public void visitElement(XmlContext context, Element element) {
        if (null != element) {
            String width = element.getAttributeNS(SdkConstants.ANDROID_URI, "layout_width");
            String height = element.getAttributeNS(SdkConstants.ANDROID_URI, "layout_height");
            if (WRAP_CONTENT.equalsIgnoreCase(width) &&
                    WRAP_CONTENT.equalsIgnoreCase(height)) {
                context.report(BothWrapContentDetector.ISSUE,
                        context.getLocation(element),
                        String.format("For %s，please avoid using WRAP_CONTENT both in layout_width and layout_height",
                                element.getNodeName()));
            }
        }
    }
}
