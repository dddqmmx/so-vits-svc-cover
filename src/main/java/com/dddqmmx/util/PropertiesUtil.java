package com.dddqmmx.util;

import com.dddqmmx.SoVitsSvcCover;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesUtil {
    public static Properties properties = new Properties();
    public static String configPath = SoVitsSvcCover.INSTANCE.getConfigFolderPath() +"/config.properties";
    public static void load(){
        try {
            FileInputStream inputStream = new FileInputStream(configPath);
            // 加载属性文件
            properties.load(inputStream);
            // 关闭输入流
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void save(){
        try {
            // 创建一个输出流来保存属性文件
            FileOutputStream outputStream = new FileOutputStream(configPath);
            // 保存属性文件
            properties.store(outputStream, "Application Configuration");
            // 关闭输出流
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
