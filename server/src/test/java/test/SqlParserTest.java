package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import edp.core.utils.SqlExtUtils;
import edp.davinci.addons.UserDataProfileContextHolder;
import edp.davinci.addons.UserDataProfileItem;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

public class SqlParserTest {

	public static void main(String[] args) throws JSQLParserException {
		
		Map<String, UserDataProfileItem> dataProfiles = new HashMap<String, UserDataProfileItem>();
		
		UserDataProfileItem profileItem = new UserDataProfileItem();
		profileItem.setName("app_id");
		profileItem.setValues(new String[]{"1001"});
		dataProfiles.put("app_id",profileItem);
		
		profileItem = new UserDataProfileItem();
		profileItem.setName("city_name");
		profileItem.setValues(new String[]{"广州","深圳"});
		dataProfiles.put("city_name",profileItem);
		
		SqlExtUtils.addFilterColumn("app_id");
		SqlExtUtils.addFilterColumn("city_name");

		UserDataProfileContextHolder.set(dataProfiles);
		
		String sql = "SELECT  `student_score_num` FROM (SELECT log.* FROM mid_netschool_course_feedback_info log) T GROUP BY `student_score_num`";
		
		sql = "SELECT city_name, sum( student_score_num) / sum( attendance ) AS 'sum@calculate@/sum(attendance)(student_score_num)', sum( student_follow_count ) / sum( student_num ) AS 'sum@calculate@/sum(student_num)(student_follow_count)', sum( report_view_count ) / sum( student_num ) AS 'sum@calculate@/sum(student_num)(report_view_count)', sum( report_view_count ) / sum( student_score_num ) AS 'sum@calculate@/sum(student_score_num)(report_view_count)' FROM( SELECT *, CASE WHEN class_average IS NULL THEN 0 ELSE class_average END 'class_average_int', cast( comeback_count_1 AS INT ) AS comeback_count_1_int, cast( comeback_count_2 AS INT ) AS comeback_count_2_int, CASE WHEN course_name LIKE \"%乐学%\" THEN '乐学' WHEN course_name LIKE \"%乐学A%\" THEN '乐学A' WHEN course_name LIKE \"%励学%\" THEN '励学' WHEN course_name LIKE \"%励学A%\" THEN '励学A' WHEN course_name LIKE \"%博学%\" THEN '博学' WHEN course_name LIKE \"%博学A%\" THEN '博学A' WHEN course_name LIKE \"%3A%\" THEN '3A' WHEN course_name LIKE \"%励学思维%\" THEN '励学思维' WHEN course_name LIKE \"%博学思维%\" THEN '博学思维' ELSE 'null' END 班型 FROM mid_netschool_course_feedback_info ) T GROUP BY city_name";
		sql = readToString("/Users/jiangwei/Desktop/1.sql");
		SqlExtUtils.rebuildSqlWithUserDataProfile(null,sql);
	}

	private static void test0() throws JSQLParserException {
		String sql = "SELECT * FROM t_user";
		Select select = (Select)CCJSqlParserUtil.parse(sql);
		PlainSelect selectBody = (PlainSelect)select.getSelectBody();
		Expression where = selectBody.getWhere();
		Table table = (Table) selectBody.getFromItem();
		EqualsTo equalsTo = new EqualsTo();
		
		Column column = new Column(table, "name");
		equalsTo.setLeftExpression(column);
		equalsTo.setRightExpression(new StringValue("11"));
		
		selectBody.setWhere(equalsTo);
		System.out.println(selectBody);
	}

	
	public static String readToString(String fileName) {  
        String encoding = "UTF-8";  
        File file = new File(fileName);  
        Long filelength = file.length();  
        byte[] filecontent = new byte[filelength.intValue()];  
        try {  
            FileInputStream in = new FileInputStream(file);  
            in.read(filecontent);  
            in.close();  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        try {  
            return new String(filecontent, encoding);  
        } catch (UnsupportedEncodingException e) {  
            System.err.println("The OS does not support " + encoding);  
            e.printStackTrace();  
            return null;  
        }  
    }
	
	
}
