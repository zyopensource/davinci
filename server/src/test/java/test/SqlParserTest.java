package test;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import edp.core.utils.SqlParserTool;
import edp.davinci.core.model.SqlFilter;
import edp.davinci.model.View;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.commons.lang.StringUtils;

import edp.core.utils.SqlExtUtils;
import edp.davinci.addons.UserDataProfileContextHolder;
import edp.davinci.addons.UserDataProfileItem;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import org.junit.Test;
import org.stringtemplate.v4.ST;
import test.base.ViewWithProjectAndSource;
import test.utils.JDBCUtils;

import javax.sql.DataSource;

public class SqlParserTest {

    public static void main1(String[] args) throws JSQLParserException {

        Map<String, UserDataProfileItem> dataProfiles = new HashMap<String, UserDataProfileItem>();

        UserDataProfileItem profileItem = new UserDataProfileItem();
        profileItem.setName("app_id");
        profileItem.setValues(new String[]{"1001"});
        dataProfiles.put("app_id", profileItem);

        profileItem = new UserDataProfileItem();
        profileItem.setName("city_name");
        profileItem.setValues(new String[]{"广州", "深圳"});
        dataProfiles.put("city_name", profileItem);

        SqlExtUtils.addFilterColumn("app_id");
        SqlExtUtils.addFilterColumn("city_name");

        UserDataProfileContextHolder.set(dataProfiles);

        String sql = "SELECT  `student_score_num` FROM (SELECT log.* FROM mid_netschool_course_feedback_info log) T GROUP BY `student_score_num`";

        sql = "SELECT city_name, sum( student_score_num) / sum( attendance ) AS 'sum@calculate@/sum(attendance)(student_score_num)', sum( student_follow_count ) / sum( student_num ) AS 'sum@calculate@/sum(student_num)(student_follow_count)', sum( report_view_count ) / sum( student_num ) AS 'sum@calculate@/sum(student_num)(report_view_count)', sum( report_view_count ) / sum( student_score_num ) AS 'sum@calculate@/sum(student_score_num)(report_view_count)' FROM( SELECT *, CASE WHEN class_average IS NULL THEN 0 ELSE class_average END 'class_average_int', cast( comeback_count_1 AS INT ) AS comeback_count_1_int, cast( comeback_count_2 AS INT ) AS comeback_count_2_int, CASE WHEN course_name LIKE \"%乐学%\" THEN '乐学' WHEN course_name LIKE \"%乐学A%\" THEN '乐学A' WHEN course_name LIKE \"%励学%\" THEN '励学' WHEN course_name LIKE \"%励学A%\" THEN '励学A' WHEN course_name LIKE \"%博学%\" THEN '博学' WHEN course_name LIKE \"%博学A%\" THEN '博学A' WHEN course_name LIKE \"%3A%\" THEN '3A' WHEN course_name LIKE \"%励学思维%\" THEN '励学思维' WHEN course_name LIKE \"%博学思维%\" THEN '博学思维' ELSE 'null' END 班型 FROM mid_netschool_course_feedback_info ) T GROUP BY city_name";
//		sql = readToString("/Users/jiangwei/Desktop/1.sql");
        SqlExtUtils.rebuildSqlWithUserDataProfile(null, sql);
    }


    public static void main(String[] args) throws JSQLParserException {
        String sql = "SELECT * FROM t_user";
        sql = "SELECT city_name, sum( student_score_num) / sum( attendance ) AS 'sum@calculate@/sum(attendance)(student_score_num)', sum( student_follow_count ) / sum( student_num ) AS 'sum@calculate@/sum(student_num)(student_follow_count)', sum( report_view_count ) / sum( student_num ) AS 'sum@calculate@/sum(student_num)(report_view_count)', sum( report_view_count ) / sum( student_score_num ) AS 'sum@calculate@/sum(student_score_num)(report_view_count)' FROM( SELECT *, CASE WHEN class_average IS NULL THEN 0 ELSE class_average END 'class_average_int', cast( comeback_count_1 AS INT ) AS comeback_count_1_int, cast( comeback_count_2 AS INT ) AS comeback_count_2_int, CASE WHEN course_name LIKE \"%乐学%\" THEN '乐学' WHEN course_name LIKE \"%乐学A%\" THEN '乐学A' WHEN course_name LIKE \"%励学%\" THEN '励学' WHEN course_name LIKE \"%励学A%\" THEN '励学A' WHEN course_name LIKE \"%博学%\" THEN '博学' WHEN course_name LIKE \"%博学A%\" THEN '博学A' WHEN course_name LIKE \"%3A%\" THEN '3A' WHEN course_name LIKE \"%励学思维%\" THEN '励学思维' WHEN course_name LIKE \"%博学思维%\" THEN '博学思维' ELSE 'null' END 班型 FROM mid_student_test.mid_netschool_course_feedback_info ) T GROUP BY city_name";
        sql = "SELECT\n" +
                "case when A.clanum>6 then '是' else '否' end '是否6讲以上',\n" +
                "concat(A.clayear,'-01-01') as '日期',\n" +
                "A.*,\n" +
                "B.Department_Long_Name\n" +
                "FROM\n" +
                "  dwd.dwd_tms_tb_clazz_detail_data_collect  A\n" +
                "  LEFT  JOIN   ods.ods_edw_v_zzysb_view  B\n" +
                "  ON A.deptName=B.school\n" +
                "  LEFT  JOIN   ods.dddd  c\n" +
                "  ON A.deptName=c.school";
//		Select select = (Select)CCJSqlParserUtil.parse(sql);
//		PlainSelect selectBody = (PlainSelect)select.getSelectBody();
//		Expression where = selectBody.getWhere();
//		PlainSelect selectBody1 = (PlainSelect)selectBody.getFromItem();
//
//		Table table = (Table) selectBody1.getFromItem();
//		EqualsTo equalsTo = new EqualsTo();
//
//		Column column = new Column(table, "name");
//		equalsTo.setLeftExpression(column);
//		equalsTo.setRightExpression(new StringValue("11"));
//
//		selectBody.setWhere(equalsTo);
//		System.out.println(selectBody);


        List<String> columns = new ArrayList<>();
        columns.add("depart");
        columns.add("depart1");
        List<String> values = new ArrayList<>();
        values.add("values1");
        values.add("values2");
        SqlFilter sqlFilter = new SqlFilter();
        SqlParserTool.SqlType sqlType = SqlParserTool.getSqlType(sql);
        if (sqlType.equals(SqlParserTool.SqlType.SELECT)) {
            Expression newExpression = null;
            Select statement = (Select) SqlParserTool.getStatement(sql);
            PlainSelect selectBody = (PlainSelect) statement.getSelectBody();
            Table table = (Table) SqlParserTool.getFromItem(selectBody);
            newExpression = appendDataProfileCondition(table, selectBody.getWhere(), columns, values);
            selectBody.setWhere(newExpression);
            List<Join> joins = SqlParserTool.getJoins(selectBody);
            for (Join join : joins) {
                table = (Table) join.getRightItem();

                newExpression = appendDataProfileCondition(table, join.getOnExpression(), columns, values);
                join.setOnExpression(newExpression);

            }
            List<Table> tables = SqlParserTool.getIntoTables(statement.getSelectBody());
//            Table table = (Table) selectBody.getFromItem();
            System.out.println(selectBody.toString());
        }
    }

    private static Expression appendDataProfileCondition(Table table, Expression orginExpression, List<String> columns, List<String> values) {
        Expression newExpression = orginExpression;
        for (String columnName : columns) {
            Column column = new Column(table, columnName);
            RegExpMySQLOperator regExpMySQLOperator = new RegExpMySQLOperator(RegExpMatchOperatorType.MATCH_CASEINSENSITIVE);
            regExpMySQLOperator.setLeftExpression(column);
            regExpMySQLOperator.setRightExpression(new StringValue(StringUtils.join(values, "|")));
            newExpression = newExpression == null ? regExpMySQLOperator : new AndExpression(newExpression, regExpMySQLOperator);
        }


        return newExpression;
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


    @Test
    public void sqlTest() throws Exception {
        Connection conn = JDBCUtils.getConnection("jdbc:mysql://10.2.3.63:13306/davinci", "davinci", "Q6NMTjzUvwmv6Syc");
        try {
            Class.forName("com.mysql.jdbc.Driver");// 动态加载mysql驱动
            System.out.println("成功加载MySQL驱动程序");
            // Statement里面带有很多方法，比如executeUpdate可以实现插入，更新和删除等
            Statement stmt = conn.createStatement();
            String sql = "SELECT v.project_id,v.source_id,v.sql," +
                    "               p.`name`   project_name," +
                    "               s.`name`        source_name," +
                    "               s.`config`     source_config" +
                    "        FROM `view` v" +
                    "                 LEFT JOIN project p on p.id = v.project_id" +
                    "                 LEFT JOIN source s on s.id = v.source_id";
            // 结果集
            ResultSet rs = stmt.executeQuery(sql);
            List<ViewWithProjectAndSource> views = JDBCUtils.Populate(rs, ViewWithProjectAndSource.class);
            List<Project> datas = new ArrayList<>();

            List<Long> projectIds = views.stream().map(v -> v.getProjectId()).distinct().collect(Collectors.toList());
            for (Long projectId : projectIds) {
                Project project = new Project();
                List<Db> dbs = new ArrayList<>();
                project.setProjectId(projectId);
                List<ViewWithProjectAndSource> pView = views.stream().filter(v -> v.getProjectId() == projectId).collect(Collectors.toList());
                List<Long> sourceIds = pView.stream().map(v -> v.getSourceId()).distinct().collect(Collectors.toList());
                for (Long sourceId : sourceIds) {
                    Db db = new Db();
                    db.setSourceId(sourceId);
                    Set<String> tables = new HashSet();
                    pView.stream().filter(v -> v.getSourceId() == sourceId).forEach(v -> {
                        Select statement = null;
                        try {
                            statement = (Select) SqlParserTool.getStatement(v.getSql());
                        } catch (JSQLParserException e) {
                            e.printStackTrace();
                        }
                        List<String> tableList = SqlParserTool.getTableList(statement);
                        tables.addAll(tableList);
                    });
                    db.setTables(new ArrayList<>(tables));
                    dbs.add(db);
                }
                project.setDbs(dbs);
                datas.add(project);
            }
            System.out.println(datas);
        } catch (SQLException e) {
            System.out.println("MySQL操作错误");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            conn.close();
        }

    }


    @Data
    class Project{
        private Long projectId;
        private List<Db> dbs;
    }
    @Data
    class Db{
        private Long sourceId;
        private List<String> tables;
    }
}
