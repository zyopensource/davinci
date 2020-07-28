package test;

import edp.davinci.core.common.Constants;
import edp.davinci.core.enums.types.DateTypeEnum;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserTest {

    public static void main(String[] args) throws ParseException {
//        System.out.println(BCrypt.hashpw("ZY.com_168", BCrypt.gensalt()));
//        String[] q = Arrays.stream(DateTypeEnum.class.getEnumConstants()).map(Enum::name).toArray(String[]::new);
//
//        String time = "2020-06-08 00:00:00";
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Calendar c = Calendar.getInstance();
//
//        //过去七天
//        c.setTime(new Date());
//        c.add(Calendar.DATE, -7);
//        Date d = c.getTime();
//        String day = format.format(d);
//        System.out.println("过去七天：" + day);
//
//        //过去一月
//        c.setTime(new Date());
//        c.add(Calendar.MONTH, -1);
//        Date m = c.getTime();
//        String mon = format.format(m);
//        System.out.println("过去一个月：" + mon);
//
//        //过去三个月
//        c.setTime(new Date());
//        c.add(Calendar.MONTH, -3);
//        Date m3 = c.getTime();
//        String mon3 = format.format(m3);
//        System.out.println("过去三个月：" + mon3);
//
//        //过去一年
//        c.setTime(format.parse(time));
//        c.add(Calendar.YEAR, -1);
//        Date y = c.getTime();
//        String year = format.format(y);
//        System.out.println("过去一年：" + year);
//
//        String line = "sum@qoq@(ssss)";
//        String pattern = "@(.*)@";
//
//        // 创建 Pattern 对象
//        Pattern r = Pattern.compile(pattern);
//
//        // 现在创建 matcher 对象
//        Matcher matcher = r.matcher(line);
//        if (matcher.find()) {
//            System.out.println("Found value: " + matcher.group(0));
//            System.out.println("Found value: " + matcher.group(1));
//        } else {
//            System.out.println("NO MATCH");
//        }

        System.out.println(getDateFormatStr("2020-09-08 00"));

    }

    public static boolean isDate(String date) {
        String path="\\d{4}(/|-)\\d{2}(/|-)\\d{2}";//定义匹配规bai则
        Pattern p=Pattern.compile(path);//实例化Pattern
        Matcher m=p.matcher(date);//验证字符串du内容是否合法
        if(m.matches()){
            return true;
        }
        return false;
    }
    public static String getDateFormatStr(String date) {
        String dateFormat = null;
        for (String str : Constants.DATE_FORMATS) {
            SimpleDateFormat format = new SimpleDateFormat(str);
            try {
                format.setLenient(false);
                format.parse(date);
                dateFormat = str;
                break;
            } catch (ParseException e) {
                continue;
            }

        }
        return dateFormat;
    }
}
