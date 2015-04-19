package managers;


import com.google.common.collect.Lists;
import parse.ParserConfig;
import sheetparser.SheetParser;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ParserManager {

    private static ParserManager pm;

    public static void initParserConfigManager(List<ParserConfig> parserConfigs) {
        if (pm == null) pm = new ParserManager(parserConfigs);
    }

    public static ParserManager getParserConfigManager() throws Exception {
        if (pm == null)
            throw new Exception("ParserConfigManager needs to be initialized with a list of parser configs");
        return pm;
    }

    private final List<ParserConfig> parserConfigs;
    private List<String> directoriesIndex;

    private ParserManager(List<ParserConfig> parserConfigs) {
        this.parserConfigs = parserConfigs;
    }


    public List<String> getDirectories() {
        if (directoriesIndex == null) {
            directoriesIndex = Lists.newArrayList();
            for (ParserConfig pc : parserConfigs) directoriesIndex.add(pc.inputPath);
        }

        return directoriesIndex;
    }


    public SheetParser getSheetParser(String directory, String fileName) throws Exception {
        ParserConfig parserConfig = null;

        for (ParserConfig pc : parserConfigs)
            if (pc.inputPath.equals(directory))
                if (fileName.matches(pc.regex)) {
                    parserConfig = pc;
                    break;
                }

        if (parserConfig == null)
            throw new Exception("No parser found for this directory: " + directory + " and filename: " + fileName);

        return loadParser(parserConfig);
    }

    private SheetParser loadParser(ParserConfig parserConfig) throws Exception {
        File jarFile = new File(parserConfig.jarPath);

        List<String> allClassesInJar = loadClassesInJar(jarFile);

        Class<?> clazz = null;

        for (String classFile : allClassesInJar) {
            Class<?> clazzTry;

            ClassLoader loader = URLClassLoader.newInstance(new URL[]{new URL("file:" + parserConfig.jarPath)});
            clazzTry = Class.forName(classFile, true, loader);

            if (SheetParser.class.isAssignableFrom(clazzTry) && !(clazzTry.equals(SheetParser.class))) {
                clazz = clazzTry;
                break;
            }
        }

        assert clazz != null;

        if (parserConfig.constructorArguments.length == 0) {
            return (SheetParser) clazz.newInstance();
        } else {
            Class parameters[] = new Class[parserConfig.constructorArguments.length];
            for (int i = 0; i < parameters.length; i++) parameters[i] = String.class;
            return (SheetParser) clazz.getConstructor(parameters).newInstance(parserConfig.constructorArguments);
        }


    }

    private List<String> loadClassesInJar(File file) throws Exception {
        List<String> found = Lists.newArrayList();

        if (file == null)
            throw new Exception("jarFile is null");

        if (!file.exists())
            throw new Exception("Jar file missing: " + file.getAbsolutePath());

        if (file.getName().endsWith(".jar")) {

            JarFile jarFile = new JarFile(file);
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    String name = entry.getName();
                    name = name.substring(0, name.lastIndexOf(".class"));
                    if (name.contains("/")) name = name.replaceAll("/", ".");
                    if (name.contains("\\")) name = name.replaceAll("\\\\", ".");
                    found.add(name);
                }

            }
        } else
            throw new Exception("Jar file isn't a jar: " + file.getAbsolutePath());

        return found;

    }

    public String getSourceSystem(String directory, String fileName) throws Exception {
        for (ParserConfig pc : parserConfigs)
            if (pc.inputPath.equals(directory))
                if (fileName.matches(pc.regex)) {
                    return pc.sourceSystem;
                }
        throw new Exception("No parser found for this directory: " + directory + " and filename: " + fileName);
    }
}
