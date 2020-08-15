package test.utils;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JDBC 工具类
 *
 * @author
 */
public class JDBCUtils {

    public static Connection connection = null;
    public static PreparedStatement preparedStatement = null;
    public static ResultSet resultSet = null;

    public static String[] types1 = {"int", "java.lang.String", "boolean", "char",
            "float", "double", "long", "short", "byte"};
    public static String[] types2 = {"Integer", "java.lang.String", "java.lang.Boolean",
            "java.lang.Character", "java.lang.Float", "java.lang.Double",
            "java.lang.Long", "java.lang.Short", "java.lang.Byte"};

    /**
     * 连接数据库
     *
     * @return
     */
    public static Connection getConnection(String url,String user,String password) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(url, user, password);
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * 关闭资源
     */
    public static void close() {
        try {
            if (connection != null) {
                connection.close();
                connection = null;
            }
            if (preparedStatement != null) {
                preparedStatement.close();
                preparedStatement = null;
            }
            if (resultSet != null) {
                resultSet.close();
                resultSet = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * 将结果集转换成实体对象集合
     *
     * @param rs 结果集
     * @param cc 实体对象映射类
     * @return
     * @throws SQLException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static List Populate(ResultSet rs, Class cc) throws SQLException, InstantiationException, IllegalAccessException {

        //结果集 中列的名称和类型的信息
        ResultSetMetaData rsm = rs.getMetaData();
        int colNumber = rsm.getColumnCount();
        List list = new ArrayList();
        Field[] fields = cc.getDeclaredFields();

        //遍历每条记录
        while (rs.next()) {
            //实例化对象
            Object obj = cc.newInstance();
            //取出每一个字段进行赋值
            for (int i = 1; i <= colNumber; i++) {
                Object value = rs.getObject(i);
                //匹配实体类中对应的属性
                for (int j = 0; j < fields.length; j++) {
                    Field f = fields[j];
                   String c = rsm.getColumnName(i);
                   String c1 = humpToLine(f.getName());
                    if (humpToLine(f.getName()).equals(rsm.getColumnName(i))) {
                        boolean flag = f.isAccessible();
                        f.setAccessible(true);
                        f.set(obj, value);
                        f.setAccessible(flag);
                        break;
                    }
                }

            }
            list.add(obj);
        }
        return list;
    }
    /** 驼峰转下划线,效率比上面高 */
    public static String humpToLine(String str) {
        Pattern humpPattern = Pattern.compile("[A-Z]");
        Matcher matcher = humpPattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

}
