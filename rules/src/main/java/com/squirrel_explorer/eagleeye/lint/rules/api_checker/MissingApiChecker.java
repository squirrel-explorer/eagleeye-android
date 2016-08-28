package com.squirrel_explorer.eagleeye.lint.rules.api_checker;

import com.android.annotations.NonNull;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import lombok.ast.AstVisitor;

/**
 * Created by squirrel-explorer on 2016-06-03.
 */
public class MissingApiChecker extends Detector implements Detector.JavaScanner {
    // 0，解析Android SDK中@removed和@hide的方法
    // 1，校验MethodInvocation是否属于Android SDK中@removed和@hide的方法
    public static final int MODE = 1;

    private MissingApiDatabase mMissingApiDb;

    public static final Issue ISSUE = Issue.create(
            "MissingApiChecker",
            "Check if some API is @removed/@hide in some versions of Android SDK",
            "In various versions of Android SDK, some APIs may be removed, so it is necessary to check it",
            Category.CORRECTNESS,
            8,
            Severity.IGNORE,
            new Implementation(
                    MissingApiChecker.class,
                    Scope.JAVA_FILE_SCOPE));

    @Override
    public void beforeCheckProject(@NonNull Context context) {
        if (1 == MissingApiChecker.MODE) {
            if (null == mMissingApiDb) {
                String missingApiDbDir = context.getProject().getDir() + "/build/missing_api_database";
                mMissingApiDb = MissingApiDatabase.getInstance(missingApiDbDir);
            }
        }
    }

    @Override
    public AstVisitor createJavaVisitor(@NonNull JavaContext context) {
        MissingApiCheckAstVisitor astVisitor = new MissingApiCheckAstVisitor(context);

        if (1 == MissingApiChecker.MODE) {
            astVisitor.setMissingApiDb(mMissingApiDb);
        }

        return astVisitor;
    }

    @Override
    public void afterCheckProject(@NonNull Context context) {
        if (0 == MissingApiChecker.MODE) {
            generateXmlResult(context, "removed-methods", MissingApiCheckAstVisitor.removedMethods);
            generateXmlResult(context, "hide-methods", MissingApiCheckAstVisitor.hideMethods);
        }
    }

    private void generateXmlResult(@NonNull Context context, @NonNull String tag, @NonNull HashMap<String, ArrayList<String>> methodSet) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();

            String apiLevel = getApiLevel(context);

            Element elementRoot = doc.createElement(tag);
            elementRoot.setAttribute("version", apiLevel);
            doc.appendChild(elementRoot);

            for (String clazz : methodSet.keySet()) {
                Element elementClazz = doc.createElement("class");
                elementClazz.setAttribute("name", clazz);

                for (String method : methodSet.get(clazz)) {
                    Element elementMethod = doc.createElement("method");
                    elementMethod.setAttribute("signature", method);

                    elementClazz.appendChild(elementMethod);
                }

                elementRoot.appendChild(elementClazz);
            }

            Properties properties = new Properties();
            properties.setProperty(OutputKeys.INDENT, "yes");
            properties.setProperty(OutputKeys.MEDIA_TYPE, "xml");
            properties.setProperty(OutputKeys.VERSION, "1.0");
            properties.setProperty(OutputKeys.ENCODING, "utf-8");
            properties.setProperty(OutputKeys.METHOD, "xml");
            properties.setProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            properties.setProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperties(properties);

            DOMSource domSource = new DOMSource(doc.getDocumentElement());
            OutputStream output = new FileOutputStream(tag + "-" + apiLevel + ".xml");
            StreamResult result = new StreamResult(output);
            transformer.transform(domSource, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getApiLevel(@NonNull Context context) {
        String prefix = "android-";
        String apiLevel = null;
        for (File srcPath : context.getMainProject().getJavaSourceFolders()) {
            apiLevel = srcPath.getName();
            if (null != apiLevel && apiLevel.startsWith(prefix)) {
                apiLevel = apiLevel.substring(prefix.length());
                break;
            }
        }
        if (null == apiLevel) {
            apiLevel = "";
        }
        return apiLevel;
    }
}
