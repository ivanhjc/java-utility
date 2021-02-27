package net.ivanhjc.utility.reflection;

import net.ivanhjc.utility.data.StringUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class ReflectionUtils {

    public static Class getClass(String classPath, String className) throws MalformedURLException, ClassNotFoundException {
        File file = new File(classPath);
        URL url = file.toURI().toURL();
        URL[] urls = new URL[]{url};
        ClassLoader cl = new URLClassLoader(urls);
        return cl.loadClass(className);
    }

    public static List<Class> getClasses(List<String> classPaths, List<String> packages) throws MalformedURLException, ClassNotFoundException {
        List<Class> classes = new ArrayList<>();
        List<URL> urls = new ArrayList<>();
        for (int i = 0; i < classPaths.size(); i++) {
            urls.add(new File(classPaths.get(i)).toURI().toURL());
        }
        ClassLoader cl = new URLClassLoader(urls.toArray(new URL[0]));
        for (String classPath : classPaths) {
            for (String p : packages) {
                File dir = new File(classPath + "/" + p.replace(".", "/"));
                File[] files = dir.listFiles();
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        if (file.isFile()) {
                            String className = p + "." + StringUtils.substringBefore(file.getName(), ".class");
                            classes.add(cl.loadClass(className));
                        }
                    }
                }
            }
        }
        return classes;
    }
}
