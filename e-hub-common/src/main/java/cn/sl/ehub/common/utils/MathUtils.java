package cn.sl.ehub.common.utils;

import org.apache.commons.lang3.StringUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by enn on 2018/1/8.
 */
public class MathUtils {

    static ScriptEngine jse = new ScriptEngineManager().getEngineByName("JavaScript");

    /*
     *偏差量：实际量-申报量,
     */
    public static String offsetQuantity(String accountQuantity, String declareQuantity) {
        if (StringUtils.isBlank(accountQuantity) || StringUtils.isBlank(declareQuantity)) {
            return "- -";
        }
        BigDecimal accountDecimal = new BigDecimal(accountQuantity);
        BigDecimal declareDecimal = new BigDecimal(declareQuantity);
        BigDecimal sub = sub(accountDecimal, declareDecimal);

        return String.valueOf(towDecimal(sub));
    }

    //偏差率：（实际量-申报量）/申报*100%
    public static String rate(String accountQuantity, String declareQuantity) {
        if (StringUtils.isBlank(accountQuantity) || StringUtils.isBlank(declareQuantity)) {
            return "- -";
        }
        String offsetQuantity = offsetQuantity(accountQuantity, declareQuantity);
        if (!StringUtils.equalsIgnoreCase("- -", offsetQuantity)) {
            BigDecimal offsetDecimal = new BigDecimal(offsetQuantity);
            BigDecimal declarQuantity = new BigDecimal(declareQuantity);
            BigDecimal divide = divide(offsetDecimal, declarQuantity);
            BigDecimal mul = mul(divide, new BigDecimal(100));
            return String.valueOf(towDecimal(mul));
        }
        return null;
    }

    /**
     * 提供精确的减法运算。  BigDecimal
     *
     * @param v1 被减数
     * @param v2 减数
     * @return 两个参数的差
     */
    public static BigDecimal sub(BigDecimal v1, BigDecimal v2) {
        if (v1 != null && v2 != null) {
            return v1.subtract(v2);
        } else {
            if (v1 != null) {
                return v1;
            }
            if (v2 != null) {
                return v2;
            }
        }
        return null;
    }

    public static BigDecimal sub(Double v1, Double v2) {
        if (null == v1 || null == v2) {
            return null;
        } else {
            return sub(new BigDecimal(v1), new BigDecimal(v2));
        }
    }

    public static Double subDouble(Double v1, Double v2, int point) {
        if (null == v1 || null == v2) {
            return null;
        } else {
            return doublePoint(v1 - v2, point);
        }
    }

    public static Double subDoubleZero(Double v1, Double v2, int point) {
        if (null == v1 || null == v2) {
            return 0D;
        } else {
            return doublePoint(v1 - v2, point);
        }
    }

    public static Double subDouble(Double v1, Double v2) {
        if (null == v1 || null == v2) {
            return null;
        }
        return v1 - v2;
    }

    public static Double subDoubleABS(Double v1, Double v2) {
        if (null == v1 || null == v2) {
            return null;
        }
        return Math.abs(v1) - Math.abs(v2);
    }

    public static Double subABSDoubleZero(Double v1, Double v2) {
        if (null == v1) {
            v1 = 0D;
        }
        if (null == v2) {
            v2 = 0D;
        }
        return Math.abs(v1 - v2);
    }

    /**
     * 提供精确的乘法运算。
     *
     * @param v1 被乘数
     * @param v2 乘数
     * @return 两个参数的积
     */
    public static BigDecimal mul(BigDecimal v1, BigDecimal v2) {
        if (v1 != null && v2 != null) {
            return v1.multiply(v2);
        } else {
            if (v1 != null) {
                return v1;
            }
            if (v2 != null) {
                return v2;
            }
        }
        return null;
    }

    /**
     * 提供精确的乘法运算。
     *
     * @param v1 被乘数
     * @param v2 乘数
     * @return 两个参数的积
     */
    public static BigDecimal mulNull(BigDecimal v1, BigDecimal v2) {
        if (v1 != null && v2 != null) {
            return v1.multiply(v2);
        }
        return null;
    }

    /**
     * 提供精确的乘法运算。
     *
     * @param v1 被乘数
     * @param v2 乘数
     * @return 两个参数的积
     */
    public static BigDecimal mulNull(BigDecimal v1, BigDecimal v2, int point) {
        if (v1 != null && v2 != null) {
            return point(v1.multiply(v2), point);
        }
        return null;
    }

    /**
     * 提供精确的乘法运算。
     *
     * @param v1 被乘数
     * @param v2 乘数
     * @return 两个参数的积
     */
    public static BigDecimal mulZero(BigDecimal v1, BigDecimal v2) {
        if (v1 != null && v2 != null) {
            return v1.multiply(v2);
        } else {
            return new BigDecimal("0");
        }
    }

    /**
     * 乘法运算  返回值保留小数（参数）
     *
     * @param v1
     * @param v2
     * @return
     */
    public static BigDecimal mul(BigDecimal v1, BigDecimal v2, int point) {
        if (v1 != null && v2 != null) {
            return point(v1.multiply(v2), point);
        }
        return null;
    }

    /**
     * 乘法运算  返回值保留小数（参数）
     *
     * @param v1
     * @param v2
     * @return
     */
    public static Double mulDoubleZero(Double v1, Double v2, int point) {
        if (v1 != null && v2 != null) {
            return doublePoint(v1 * v2, point);
        }
        return 0D;
    }

    /**
     * 乘法运算  返回值保留小数（参数）
     *
     * @param v1
     * @param v2
     * @return
     */
    public static Double mulDoubleNull(Double v1, Double v2, int point) {
        if (null == v1 || null == v2) {
            return null;
        }
        return doublePoint(v1 * v2, point);
    }

    /**
     * 乘法运算  返回值保留小数（参数）
     * 非四舍五入
     *
     * @param v1
     * @param v2
     * @return
     */
    public static Double mulDoubleNullNotRounding(Double v1, Double v2, int point) {
        if (null == v1 || null == v2) {
            return null;
        }
//        Double value = v1 * v2;
//        Integer pointValue = 1;
//        for (int i = 0; i < point; i++) {
//            pointValue = pointValue * 10;
//        }
//        Long valueLong = valueDouble.longValue();
//        BigDecimal respValue = valueDouble.divide(b2);
//        bigDecimalToDouble(respValue, 0);
//        return doublePoint(Double.valueOf(valueLong) / Double.valueOf(pointValue), point);
        BigDecimal b1 = new BigDecimal(v1 + "");
        BigDecimal b2 = new BigDecimal(v2 + "");
        BigDecimal valueDouble = b1.multiply(b2).setScale(point, RoundingMode.DOWN);
        return valueDouble.doubleValue();
    }

    /**
     * 乘法运算  返回值保留小数（参数）
     * 非四舍五入
     *
     * @param v1
     * @return
     */
    public static Double doublePointNotRounding(Double v1, int point) {
        if (null == v1) {
            return null;
        }
        BigDecimal b1 = new BigDecimal(v1 + "");
        BigDecimal valueDouble = b1.setScale(point, RoundingMode.DOWN);
        return valueDouble.doubleValue();
    }

    /**
     * 保留两位小数  BigDecimal
     *
     * @param v1
     * @return
     */
    public static BigDecimal towDecimal(BigDecimal v1) {
        if (v1 != null) {
            BigDecimal decimal = v1.setScale(2, BigDecimal.ROUND_HALF_UP);
            return decimal;
        } else {
            return null;
        }
    }

    /**
     * 保留两位小数  BigDecimal
     * 0.00000保留成0.00
     *
     * @param v1
     * @return
     */
    public static BigDecimal towDecimaltwo(BigDecimal v1) {
        if (v1 != null) {
            if (v1.equals("0.00000")) {
                return new BigDecimal(0.00);
            } else {
                BigDecimal decimal = v1.setScale(2, BigDecimal.ROUND_HALF_UP);
                return decimal;
            }
        } else {
            return null;
        }
    }

    /**
     * 保留两位小数  BigDecimal
     *
     * @param value
     * @return
     */
    public static BigDecimal twoDecimal(BigDecimal value) {
        if (null == value) {
            value = new BigDecimal(0);
        }
        return value.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 保留两位小数  BigDecimal
     *
     * @param value NULL保留null
     * @return
     */
    public static BigDecimal twoDecimalNullToNull(BigDecimal value) {
        if (null == value) {
            value = null;
        }
        return value.setScale(2, BigDecimal.ROUND_HALF_UP);
    }


    /**
     * 提供精确的除法运算。  BigDecimal
     *
     * @param v1
     * @param v2
     * @return
     */
    public static BigDecimal divide(BigDecimal v1, BigDecimal v2) {
        if (v1 != null && v2 != null) {
            if (compare(v2, new BigDecimal(0))) {
                return v1.divide(v2, 4, BigDecimal.ROUND_HALF_UP);
            }
        } else {
            if (v1 != null) {
                return v1;
            }
            if (v2 != null) {
                return v2;
            }
        }
        return null;
    }

    /**
     * 提供精确的除法运算。  BigDecimal
     *
     * @param v1
     * @param v2
     * @return
     */
    public static BigDecimal divideZero(BigDecimal v1, BigDecimal v2) {
        if (v1 != null && v2 != null) {
            if (compare(v2, new BigDecimal(0))) {
                return v1.divide(v2, 4, BigDecimal.ROUND_HALF_UP);
            }
        } else {
            return new BigDecimal("0");
        }
        return new BigDecimal("0");
    }

    /**
     * 提供精确的除法运算。  BigDecimal
     *
     * @param v1
     * @param v2
     * @return
     */
    public static Double divideZero(Double v1, Double v2, Integer point) {
        if (null == v1 || null == v2 || v1 == 0D || v2 == 0D) {
            return 0D;
        }
        BigDecimal b1 = new BigDecimal(String.valueOf(v1));
        BigDecimal b2 = new BigDecimal(String.valueOf(v2));
        return b1.divide(b2, point, BigDecimal.ROUND_HALF_UP).doubleValue();

    }

    /**
     * 提供精确的除法运算。  BigDecimal
     *
     * @param v1
     * @param v2
     * @return
     */
    public static BigDecimal divideNull(BigDecimal v1, BigDecimal v2) {
        if (v1 != null && v1.compareTo(new BigDecimal("0")) == 0) {
            return BigDecimal.ZERO;
        }

        if (v1 != null && v2 != null) {
            if (compare(v2, new BigDecimal(0))) {
                return v1.divide(v2, 4, BigDecimal.ROUND_HALF_UP);
            }
        } else {
            return null;
        }
        return null;
    }

    /**
     * 提供精确的除法运算
     *
     * @param v1
     * @param v2
     * @return
     */
    public static Double divideNullNotRounding(Double v1, Double v2, Integer point) {
        if (null == v1 || null == v2) {
            return null;
        }
        if (v1 == 0D || v2 == 0D) {
            return 0D;
        }
        BigDecimal b1 = new BigDecimal(v1 + "");
        BigDecimal b2 = new BigDecimal(v2 + "");
        BigDecimal valueDouble = b1.divide(b2, point, BigDecimal.ROUND_DOWN);
        return valueDouble.doubleValue();
    }

    /**
     * 提供精确的除法运算
     *
     * @param v1
     * @param v2
     * @return
     */
    public static Double divideNull(Double v1, Double v2, Integer point) {
        if (null == v1 || null == v2) {
            return null;
        }
        if (v1 == 0D || v2 == 0D) {
            return 0D;
        }
        BigDecimal b1 = new BigDecimal(String.valueOf(v1));
        BigDecimal b2 = new BigDecimal(String.valueOf(v2));
        return b1.divide(b2, point, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 加法运算
     *
     * @param v1
     * @param v2
     * @return
     */
    public static Double addDouble(Double v1, Double v2, Integer point) {
        if (null == v1) {
            return doublePoint(v2, point);
        }
        if (null == v2) {
            return doublePoint(v1, point);
        }
        return doublePoint(v1 + v2, point);
    }

    /**
     * 计算环比
     *
     * @param v1
     * @param v2
     * @return
     */
    public static double divideMulEver(BigDecimal v1, BigDecimal v2) {
        if (v1 != null && v2 != null) {
            BigDecimal v3 = sub(v1, v2);
            if (compare(v2, new BigDecimal(0))) {
                BigDecimal v4 = v3.divide(v2, 4, BigDecimal.ROUND_HALF_UP);
                return mul(v4, new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            }
        }
        return 0;
    }

    /**
     * 计算环比
     *
     * @param v1
     * @param v2
     * @return
     */
    public static BigDecimal divideMul(BigDecimal v1, BigDecimal v2) {
        if (v1 != null && v2 != null) {
            BigDecimal v3 = sub(v1, v2);
            if (compare(v2, new BigDecimal(0))) {
                BigDecimal v4 = v3.divide(v2, 4, BigDecimal.ROUND_HALF_UP);
                return mul(v4, new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
        } else if (v1 == null && v2 == null) {
            return null;
        }
        return BigDecimal.ZERO;
    }

    /**
     * 提供精确的加法运算。
     *
     * @param v1 被加数
     * @param v2 加数
     * @return 两个参数的和
     */
    public static BigDecimal add(BigDecimal v1, BigDecimal v2) {
        if (v1 != null && v2 != null) {
            return v1.add(v2);
        } else {
            if (v1 != null) {
                return v1;
            }
            if (v2 != null) {
                return v2;
            }
        }
        return null;
    }


    /**
     * 比较大小
     */
    public static boolean compare(BigDecimal b1, BigDecimal b2) {
        if (b1 == null || b2 == null) {
            return false;
        }
        int i = b1.compareTo(b2);
        if (i == -1) {
            //= -1,表示bigdemical小于bigdemical2；
            return false;
        } else if (i == 1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 比较大小
     */
    public static Double compareReturnMinABS(Double b1, Double b2) {
        if (b1 == null) {
            return b2;
        }
        if (b2 == null) {
            return b1;
        }
        if (Math.abs(b1) > Math.abs(b2)) {
            return b2;
        }
        return b1;
    }

    public static boolean eq(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) {
            return true;
        }
        int i = a.compareTo(b);
        if (i != 0) {
            return false;
        }
        return true;
    }


    /**
     * 保留 n 位小数  BigDecimal
     *
     * @param v1
     * @return
     */
    public static BigDecimal point(BigDecimal v1, int n) {
        if (v1 != null) {
            BigDecimal decimal = v1.setScale(n, BigDecimal.ROUND_HALF_UP);
            return decimal;
        } else {
            return null;
        }


    }

    public static String point(String v1, int n) {
        if (v1 != null) {
            BigDecimal decimal = new BigDecimal(v1).setScale(n, BigDecimal.ROUND_HALF_UP);
            return bigDecimalToString(decimal);
        } else {
            return null;
        }


    }

    /**
     * 保留 n 位小数  String
     *
     * @param v1
     * @return
     */
    public static String pointStr(BigDecimal v1, int n) {
        if (v1 != null) {
            BigDecimal decimal = v1.setScale(n, BigDecimal.ROUND_HALF_UP);
            return bigDecimalToString(decimal);
        } else {
            return null;
        }
    }

    /**
     * 转为万千瓦时，保留四位小数
     *
     * @param v1
     * @return
     */
    public static BigDecimal thousand(BigDecimal v1) {
        if (v1 != null) {
            BigDecimal decimal = divide(v1, new BigDecimal(10000));
            return decimal.setScale(4, BigDecimal.ROUND_HALF_UP);
        } else {
            return new BigDecimal(0);
        }
    }


    /**
     * 获取比目标值小的最大整十数
     *
     * @param needFormat
     * @return
     */
    public static Integer getMinNumFormat(BigDecimal needFormat) {
        Integer needFormat1 = needFormat.intValue();
        needFormat1 = (needFormat1 / 10) * 10;
        return needFormat1;
    }

    /**
     * 判断目标值是否处于两个值中间（左闭右开）
     *
     * @param v1
     * @param min
     * @param max
     */
    public static Boolean between(BigDecimal v1, Integer min, Integer max) {

        if ((v1.compareTo(new BigDecimal(max)) == -1) && v1.compareTo(new BigDecimal(min)) > -1) {
            return true;
        }
        return false;
    }

    /**
     * 将 BigDecimal 转换为 string
     */
    public static String bigDecimalToString(BigDecimal v1) {
        if (v1 == null) {
            return new BigDecimal(0).toString();
        } else {
            return v1.toString();
        }
    }

    public static String bigDecimalToString(BigDecimal v1, int point) {
        if (v1 == null) {
            return new BigDecimal(0).toString();
        } else {
            return point(v1, point).toString();
        }
    }

    public static String bigDecimalToStringNull(BigDecimal v1, int point) {
        if (v1 == null) {
            return null;
        } else {
            return point(v1, point).toString();
        }
    }


    /**
     * 将 Long 转换成 string
     */
    public static String longToString(Long v1) {
        if (v1 == null) {
            return null;
        } else {
            return v1.toString();
        }
    }

    /**
     * 将 BigDecimal 转换为 double  解决空指针问题
     */
    public static Double bigDecimalToDouble(BigDecimal v1) {
        if (v1 == null) {
            return 0D;
        } else {
            return Double.valueOf(v1.toString());
        }
    }

    /**
     * 将 BigDecimal 转换为 double  解决空指针问题
     */
    public static Double bigDecimalToDoubleNull(BigDecimal v1) {
        if (v1 == null) {
            return null;
        } else {
            return Double.valueOf(v1.toString());
        }
    }

    public static Double bigDecimalToDouble(BigDecimal v1, Double orelse) {
        if (v1 == null) {
            return orelse;
        } else {
            return Double.valueOf(v1.toString());
        }
    }

    public static Double bigDecimalToDouble(BigDecimal v1, int point) {
        if (v1 == null) {
            return 0D;
        } else {
            return Double.valueOf(point(v1, point).toString());
        }
    }

    public static Double aDoubletwo(Double v1, int point) {
        if (v1 == null) {
            return 0D;
        } else {
            return bigDecimalToDouble(point(new BigDecimal(v1), point));
        }
    }

    public static Double doublePoint(Double v1, int point) {
        if (v1 == null) {
            return null;
        } else {
            return point(new BigDecimal(v1), point).doubleValue();
        }
    }

    /**
     * 小数位占位
     *
     * @param v1
     * @param point
     * @return
     */
    public static String doublePointFormat(Double v1, int point) {
        if (v1 == null) {
            return null;
        } else {
            NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMaximumFractionDigits(point);
            /*
             * setMinimumFractionDigits设置成2
             *
             * 如果不这么做，那么当value的值是100.00的时候返回100
             *
             * 而不是100.00
             */
            nf.setMinimumFractionDigits(point);
            nf.setRoundingMode(RoundingMode.HALF_UP);
            /*
             * 如果想输出的格式用逗号隔开，可以设置成true
             */
            nf.setGroupingUsed(false);
            return nf.format(v1);
        }
    }

    public static String doubleToString(Double v1) {
        if (v1 == null) {
            return "0";
        } else {
            return v1.toString();
        }
    }

    /**
     * 公式计算后返回结果
     */
    public static BigDecimal scriptEngine(String f) {
        Object eval = null;
        try {
            eval = jse.eval(f);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        if (eval == null) {
            return null;
        }
        return new BigDecimal(eval.toString());
    }


    public static BigDecimal scriptEngine(String f, Map<String, String> map) {
        //处理map
        for (Map.Entry<String, String> e : map.entrySet()) {
            f = f.replaceAll(e.getKey(), e.getValue());
        }
        return scriptEngine(f);
    }


    public static BigDecimal scriptEngine(String f, Map<String, String> map, int point) {
        BigDecimal bigDecimal = scriptEngine(f, map);
        return point(bigDecimal, point);
    }


    public static BigDecimal pow(BigDecimal a, Integer b) {
        if (a != null && b != null) {
            return new BigDecimal(Math.pow(a.doubleValue(), b));
        } else {
            if (a != null) {
                return null;
            }
            if (b != null) {
                return null;
            }
        }
        return null;
    }


    public static BigDecimal sqrt(BigDecimal a) {
        if (a != null) {
            return new BigDecimal(Math.sqrt(a.doubleValue()));
        } else {
            return null;
        }
    }

    public static BigDecimal abs(BigDecimal v1) {
        if (v1 != null) {
            return new BigDecimal(Math.abs(v1.doubleValue()));
        }

        return null;
    }

    public static BigDecimal absString(BigDecimal v1) {
        if (v1 != null) {
            return new BigDecimal("" + Math.abs(v1.doubleValue()));
        }
        return null;
    }

    /**
     * 生成随机double值
     *
     * @param min
     * @param max
     * @return
     */
    public static double randomDoubleValue(double min, double max) {
        return MathUtils.aDoubletwo(Math.random() * (max - min) + min, 2);
    }

    /**
     * 生成随机double值
     *
     * @param min
     * @param max
     * @param point
     * @return
     */
    public static double randomDoubleValue(double min, double max, int point) {
        return MathUtils.aDoubletwo(Math.random() * (max - min) + min, point);
    }

    /**
     * 生成随机int值
     *
     * @param min
     * @param max
     * @return
     */
    public static int randomIntegerValue(double min, double max) {
        return MathUtils.aDoubletwo(Math.random() * (max - min) + min, 0).intValue();
    }

    /**
     * 生成随机BigDecimal值
     *
     * @param min
     * @param max
     * @return
     */
    public static BigDecimal randomBigDecimalValue(double min, double max) {
        return new BigDecimal("" + MathUtils.aDoubletwo(Math.random() * (max - min) + min, 2));
    }

    /**
     * 生成多个随机double值
     *
     * @param min
     * @param max
     * @param num
     * @return
     */
    public static List<Double> randomDoubleValueList(double min, double max, int num) {
        return IntStream.range(0, num).mapToObj(i -> randomDoubleValue(min, max)).collect(Collectors.toList());
    }

    /**
     * 生成多个BigDecimal随机值
     *
     * @param min
     * @param max
     * @param num
     * @return
     */
    public static List<BigDecimal> randomBigDecimalValueList(double min, double max, int num) {
        return IntStream.range(0, num).mapToObj(i -> randomBigDecimalValue(min, max)).collect(Collectors.toList());
    }


    public static Boolean regularMatch(String value) {
        String pattern = "[1-9]\\d*";
        return Pattern.compile(pattern).matcher(value).find();
    }

    public static Double stringToDouble(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return Double.parseDouble(value);
    }
}
