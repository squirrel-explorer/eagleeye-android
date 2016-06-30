package com.squirrel_explorer.eagleeye.lint.api_checker;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by squirrel-explorer on 16/6/24.
 */
public class MissingApiDatabase {
    private static MissingApiDatabase instance = null;

    private HashMap<Integer, HashMap<String, ArrayList<String>>> mRemovedDatabase = new HashMap<>();
    private HashMap<Integer, HashMap<String, ArrayList<String>>> mHideDatabase = new HashMap<>();

    private static final String TAG_REMOVED = "removed-methods";
    private static final String TAG_HIDE = "hide-methods";

    public static MissingApiDatabase getInstance(String dbDir) {
        if (null == instance) {
            instance = new MissingApiDatabase(dbDir);
        }
        return instance;
    }

    private MissingApiDatabase(String dbDirName) {
        File dbDir = new File(dbDirName);
        if (dbDir.exists()) {
            File[] methodFileList = dbDir.listFiles();
            if (null != methodFileList && methodFileList.length > 0) {
                for (File methodFile : methodFileList) {
                    if (methodFile.isFile() && methodFile.getName().endsWith(".xml")) {
                        parseMethodsFile(methodFile);
                    }
                }
            }
        }
    }

    private void parseMethodsFile(File methodsFile) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(methodsFile);
            Node rootNode = doc.getFirstChild();

            String apiLevelStr = getAttributeValue(rootNode, "version");
            int apiLevel = -1;
            try {
                apiLevel = Integer.parseInt(apiLevelStr);
            } catch (Exception e) {
                apiLevel = -1;
            }
            if (apiLevel < 0) {
                return;
            }

            boolean isRemoved = TAG_REMOVED.equals(rootNode.getNodeName());
            boolean isHide = TAG_HIDE.equals(rootNode.getNodeName());
            if (isRemoved || isHide) {
                NodeList clazzList = rootNode.getChildNodes(), methodList;
                Node clazz, method;
                String clazzName, methodSignature;
                if (null != clazzList) {
                    for (int i = 0; i < clazzList.getLength(); i++) {
                        clazz = clazzList.item(i);

                        clazzName = getAttributeValue(clazz, "name");
                        if (null == clazzName || clazzName.isEmpty()) {
                            continue;
                        }

                        methodList = clazz.getChildNodes();
                        if (null != methodList) {
                            for (int j = 0; j < methodList.getLength(); j++) {
                                method = methodList.item(j);

                                methodSignature = getAttributeValue(method, "signature");
                                if (null == methodSignature || methodSignature.isEmpty()) {
                                    continue;
                                }

                                if (isRemoved) {
                                    putRemoved(apiLevel, clazzName, methodSignature);
                                } else {    // isHide
                                    putHide(apiLevel, clazzName, methodSignature);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getAttributeValue(Node node, String attributeName) {
        if (null == node || !node.hasAttributes() ||
                null == attributeName) {
            return null;
        }
        Node attr = node.getAttributes().getNamedItem(attributeName);
        if (null == attr) {
            return null;
        }
        return attr.getNodeValue();
    }

    private void putRemoved(int apiLevel, String clazz, String method) {
        put(mRemovedDatabase, apiLevel, clazz, method);
    }

    private void putHide(int apiLevel, String clazz, String method) {
        put(mHideDatabase, apiLevel, clazz, method);
    }

    private void put(HashMap<Integer, HashMap<String, ArrayList<String>>> db,
                     int apiLevel, String clazz, String method) {
        HashMap<String, ArrayList<String>> methodSet;
        if (db.containsKey(apiLevel)) {
            methodSet = db.get(apiLevel);
        } else {
            methodSet = new HashMap<>();
            db.put(apiLevel, methodSet);
        }

        ArrayList<String> methods;
        if (methodSet.containsKey(clazz)) {
            methods = methodSet.get(clazz);
        } else {
            methods = new ArrayList<>();
            methodSet.put(clazz, methods);
        }
        methods.add(method);
    }

    public boolean isMissing(int apiLevel, String clazz, String method, boolean includeHide) {
        boolean result = isRemoved(apiLevel, clazz, method);
        if (includeHide) {
            result |= isHide(apiLevel, clazz, method);
        }
        return result;
    }

    public boolean isRemoved(int apiLevel, String clazz, String method) {
        return isInDB(mRemovedDatabase, apiLevel, clazz, method);
    }

    public boolean isHide(int apiLevel, String clazz, String method) {
        return isInDB(mHideDatabase, apiLevel, clazz, method);
    }

    private boolean isInDB(HashMap<Integer, HashMap<String, ArrayList<String>>> db,
                           int apiLevel, String clazz, String method) {
        if (!db.containsKey(apiLevel)) {
            return false;
        }

        HashMap<String, ArrayList<String>> methodSet = db.get(apiLevel);
        if (!methodSet.containsKey(clazz)) {
            return false;
        }

        ArrayList<String> methods = methodSet.get(clazz);
        if (!methods.contains(method)) {
            return false;
        }

        return true;
    }
}
