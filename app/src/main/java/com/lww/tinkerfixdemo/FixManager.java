package com.lww.tinkerfixdemo;

import android.content.Context;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashSet;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * 修复补丁的类
 */
public class FixManager {

    private static HashSet<File> loadDex = new HashSet<>();
    static {
        loadDex.clear();
    }
    /**
     * 修复 bug
     *
     * @param context
     */
    public static void loadDex(Context context) {
        if (context == null) {
            return;
        }
//           1：获取到 所有的补丁
        File odex = context.getDir("odex", Context.MODE_PRIVATE);
//          获取到 odex 下的所有文件
        File[] files = odex.listFiles();
//        判断是否是补丁
        for (File file : files)
            if (file.getName().startsWith("classed") || file.getName().endsWith(".dex")) {
                loadDex.add(file);
            }
//         创建一个缓存目录 （解压后的文件存储目录）
        String optimezeDir = odex.getAbsolutePath() + File.separator + "opt_dex";
        File fpot = new File(optimezeDir);
        if (!fpot.exists()) {
            fpot.mkdir();
        }
//         修复bug
        for (File dex : loadDex) {

            try {
                //            将每个dex 融入到app的dexElement 数组中
//            获取到 当前app的dexElement数组  反射获取
//            1 获取类加载器
                PathClassLoader classLoader = (PathClassLoader) context.getClassLoader();
//           获取PathClassLoader 的父类 BaseDexClassLoader
                Class<?> superclass = classLoader.getClass().getSuperclass();
//          获取 BaseDexClassLoader 的 成员变量 pathlist
                Field pathList = superclass.getDeclaredField("pathList");
                pathList.setAccessible(true);
//               执行 获取 pathList 在类加载器中的值
                Object pathListValue = pathList.get(classLoader);
//             1  现获取 Dexpathlist  在获取  dexelement 成员变量
                Class<?> dexpathlist = pathListValue.getClass();
                Field dexElements = dexpathlist.getDeclaredField("dexElements");
                dexElements.setAccessible(true);
//                获取到 当前app的 dexElements数组中的值  也就是  dex文件的数组
                Object dexElementValue = dexElements.get(pathListValue);
//               去加载补丁包的dexelement
//                动态加载 技术 使用DexClassLoader
//                第一个参数:补丁包路径  第二个参数：解压后的路径
//                第三个参数:是否需要加载libary  第四个参数；类加载器的父类  让这个类管理补丁包
                DexClassLoader dexClassLoader = new DexClassLoader(dex.getAbsolutePath(),
                        optimezeDir, null, context.getClassLoader());

                //             获取补丁中的  pathlist
                Object myPathList = pathList.get(dexClassLoader);
//                获取补丁包中的 dexelement
                Object mydexElement = dexElements.get(myPathList);
//                数组合并
//                 1 获取 当前app的 dexelement 数组长度
                int appdexlength = Array.getLength(dexElementValue);
                int myLength = Array.getLength(mydexElement);
//                 创建一个新的数组
                int newlength = appdexlength + myLength;
//                 首先 先获取数组类型
                Class<?> componentType = mydexElement.getClass().getComponentType();
                Object newDexElement = Array.newInstance(componentType, newlength);

//                给新数组 添加dex
                for (int x = 0; x < newlength; x++) {
                    if (x < myLength) {
                        Array.set(newDexElement, x, Array.get(mydexElement, x));
                    } else {
                        Array.set(newDexElement, x, Array.get(dexElementValue, x-myLength));
                    }
                }
//                将合并好的数组 赋值给 当前app的 dexement数组
                dexElements.set(pathListValue, newDexElement);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
