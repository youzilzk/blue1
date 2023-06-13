package com.youzi.blue.net.common.utils;


public class SystemUtils {
    private static final LoggerFactory log = LoggerFactory.getLogger();

    private static String currentJarPath = null;

    public static String getEnvPath() {
        if (currentJarPath == null) {
            String path = SystemUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            boolean contains = path.contains(".jar");

            String prefix;
            if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
                prefix = path.startsWith("file:/") ? "file:/" : "/";
            } else {
                prefix = "file:";
            }

            path = contains ? path.substring(prefix.length(), path.indexOf(".jar")) : path.substring(prefix.length());

            currentJarPath = contains ? path.substring(0, path.lastIndexOf("/")) : path;
        }

        log.info("当前执行文件父路径: {}", currentJarPath);
        return currentJarPath;
    }
}
