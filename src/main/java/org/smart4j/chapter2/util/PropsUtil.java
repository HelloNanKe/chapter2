package org.smart4j.chapter2.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropsUtil {

    private static final Logger LOGGER= LoggerFactory.getLogger(PropsUtil.class);

    /**
     * 加载属性文件
     * @param fileName
     * @return
     */
    public static Properties loadProps(String fileName){
        Properties properties=null;
        InputStream is=null;
        try {

            is=Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
            if(is==null){
                throw new FileNotFoundException(fileName+"file not found exception");
            }
            properties=new Properties();
            properties.load(is);
        }catch (Exception e){
            LOGGER.error("加载属性文件失败",e);
        }finally {
            if (is!=null){
                try {
                    is.close();
                } catch (IOException e) {
                    LOGGER.error("关闭输入流失败",e);
                }
            }
        }
        return properties;
    }

    /**
     * 获取字符型类型,默认为空字符串
     * @param properties
     * @param key
     * @return
     */
    public static String getString(Properties properties,String key){
        return getString(properties,key,"");
    }

    public static String getString(Properties properties,String key,String defultValue){
        String val=defultValue;
        if(properties.containsKey(val)){
            val=properties.getProperty(key);
        }
        return val;
    }

    public static int Int(Properties properties,String key,int defultValue){
        int val=defultValue;
        if(properties.containsKey(val)){
            val=CastUtil.castInt(properties.getProperty(key));
        }
        return val;
    }

    /**
     * 获取key对应的boolean value
     * @param properties
     * @param key
     * @return
     */
    public static boolean getBoolean(Properties properties,String key){
        return getBoolean(properties,key,false);
    }

    /**
     * 获取key对应的boolean value
     * @param properties
     * @param key
     * @param defaultValue
     * @return
     */
    public static boolean getBoolean(Properties properties,String key,boolean defaultValue){
        if (properties.containsKey(key)){
            return CastUtil.castBoolean(properties.get(key));
        }
        return defaultValue;
    }

}
