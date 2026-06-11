package cn.sl.ehub.common.utils;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;

import java.util.*;

/**
 * @Description: 16进制颜色编码工具类
 * @Author sl
 * @Date 2026-05-28
 */
public class RandomColorUtil {

    /**
     * 随机获取颜色
     *
     * @return
     */
    public static String getColor() {
        //红色
        String red;
        //绿色
        String green;
        //蓝色
        String blue;
        //生成随机对象
        Random random = new Random();
        //生成红色颜色代码
        red = Integer.toHexString(random.nextInt(256)).toUpperCase();
        //生成绿色颜色代码
        green = Integer.toHexString(random.nextInt(256)).toUpperCase();
        //生成蓝色颜色代码
        blue = Integer.toHexString(random.nextInt(256)).toUpperCase();

        //判断红色代码的位数
        red = red.length() == 1 ? "0" + red : red;
        //判断绿色代码的位数
        green = green.length() == 1 ? "0" + green : green;
        //判断蓝色代码的位数
        blue = blue.length() == 1 ? "0" + blue : blue;
        //生成十六进制颜色值
        return "#" + red + green + blue;
    }

    /**
     * 随机获取颜色集合其唯一
     *
     * @return
     */
    public static List<String> getColorSet(int size) {

        TreeSet<String> colorSet = new TreeSet<>();
        for (int i = 0; i < size; i++) {
            String color = getColor();
            if (colorSet.size() == 0 || !colorSet.contains(color)) {
                colorSet.add(color);
            } else {
                addNewColor(colorSet);
            }

        }
        return new ArrayList<>(colorSet);
    }

    private static void addNewColor(TreeSet<String> colorSet) {
        int begin = colorSet.size();
        colorSet.add(getColor());
        int end = colorSet.size();
        if (begin == end) {
            addNewColor(colorSet);
        }
    }

    public static void main(String[] args) {
        System.out.println(RandomColorUtil.getColorSet(5));
    }
}
