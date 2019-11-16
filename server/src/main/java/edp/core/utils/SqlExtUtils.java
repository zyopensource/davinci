package edp.core.utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edp.core.exception.SourceException;
import edp.davinci.addons.DataProfileItem;
import edp.davinci.addons.UserDataProfileContextHolder;
import edp.davinci.core.enums.LogNameEnum;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

/**
 * 
 * @author jiangwei
 *
 */
public class SqlExtUtils {

	private static final Logger optLogger = LoggerFactory.getLogger(LogNameEnum.BUSINESS_OPERATION.getName());
	
	private static final String LEFT_PARENTHESE = "(";
	private static final String RIGHT_PARENTHESE = ")";
	private static final String INNER_SQL_EXPR = "{innerSQL}";
	private static final String REGEX_BLANK = "\\s+";
	private static Pattern sqlJoinPattern = Pattern.compile("\\W+JOIN\\W+");
	//
	private static Pattern wrapperSqlPattern = Pattern.compile("\\W+FROM\\W+\\((SELECT|select).*\\)\\s+T\\s+");

	private static Map<String, List<String>> tableColumns = new HashMap<>();
	
//	static{
//		tableColumns.put("t_user", Arrays.asList("account_id","app_id"));
//		tableColumns.put("t_account", Arrays.asList("account_id","seller_id"));
//	}

	public static List<String> getColumnNames(DataSource dataSource, String tableName) {
		tableName = tableName.toLowerCase();
		List<String> columns = tableColumns.get(tableName.toLowerCase());
		if (columns == null) {
			columns = parseAndCacheColumnNames(dataSource, tableName);
		}

		return columns;
	}

	private synchronized static List<String> parseAndCacheColumnNames(DataSource dataSource, String tableName) {

		if (tableColumns.containsKey(tableName))
			return tableColumns.get(tableName);
		Connection connection = null;
		ResultSet rs = null;

		List<String> columns = new ArrayList<>();
		try {
			connection = dataSource.getConnection();
			if (null != connection) {
				DatabaseMetaData metaData = connection.getMetaData();
				rs = metaData.getPrimaryKeys(null, null, tableName);
				while (rs.next()) {
					columns.add(rs.getString(4).toLowerCase());
				}
				rs.close();

				rs = metaData.getColumns(null, null, tableName, "%");
				while (rs.next()) {
					columns.add(rs.getString(4).toLowerCase());
				}
				rs.close();
			}

			tableColumns.put(tableName, columns);
			return columns;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SourceException(e.getMessage());
		} finally {
			try {
				connection.close();
			} catch (Exception e2) {
			}
		}
	}

	public static List<String> parseTableNames(String sql) {

		List<String> result = new ArrayList<>(2);

		sql = sql.substring(sql.indexOf("FROM") + 4).trim();

		String[] tmpArr = sqlJoinPattern.split(sql);
		if (tmpArr.length == 1) {
			tmpArr = sql.split(REGEX_BLANK);
			result.add(tmpArr[0].trim().toLowerCase());
		} else {
			String[] subTmpArr;
			for (String str : tmpArr) {
				subTmpArr = str.trim().split(REGEX_BLANK);
				result.add(subTmpArr[0].trim().toLowerCase());
			}
		}

		return result;
	}
	
	public static String[] resolveWrappersql(String sql){
		Matcher matcher = wrapperSqlPattern.matcher(sql);
		if(matcher.find()){
			String innerSql = matcher.group().trim();
			innerSql = innerSql.substring(innerSql.indexOf(LEFT_PARENTHESE) + 1);
			innerSql = innerSql.substring(0,innerSql.lastIndexOf(RIGHT_PARENTHESE));
			return new String[]{innerSql,sql.replace(innerSql, INNER_SQL_EXPR)};
		}
		return new String[]{sql};
	}
	
	public static String rebuildSqlWithUserDataProfile(DataSource dataSource, String originSql){
        if(!originSql.toUpperCase().trim().startsWith("SELECT")){
        	return originSql;
        }
        //
		List<DataProfileItem> dataProfiles = UserDataProfileContextHolder.getDataProfiles();
		if(dataProfiles == null || dataProfiles.isEmpty()){
			return originSql;
		}
		
		//
		String[] sqls = SqlExtUtils.resolveWrappersql(originSql);
		Select select = null;
		try {
			select = (Select) CCJSqlParserUtil.parse(sqls[0]);
		} catch (JSQLParserException e) {
			optLogger.error("rebuildDataProfileSql_ERROR",e);
			throw new RuntimeException("sql解析错误");
		}
		PlainSelect selectBody = (PlainSelect) select.getSelectBody();
		Table table = (Table) selectBody.getFromItem();
		List<String> columnNames = SqlExtUtils.getColumnNames(dataSource, table.getName().toLowerCase());
		
		Expression newExpression = null;
		Iterator<DataProfileItem> iterator = dataProfiles.iterator();
		DataProfileItem item;
		while(iterator.hasNext()){
			item =iterator.next();
			if(!columnNames.contains(item.getFieldName().toLowerCase()))continue;
			newExpression = appendDataProfileCondition(table, selectBody.getWhere(), item);
			selectBody.setWhere(newExpression);
			//主表已经处理的条件，join表不在处理
			iterator.remove();
		}
		
		//JOIN 
		List<Join> joins = selectBody.getJoins();
		if(joins != null && !dataProfiles.isEmpty()){
			for (Join join : joins) {
				table = (Table) join.getRightItem();
				columnNames = SqlExtUtils.getColumnNames(dataSource, table.getName().toLowerCase());
				for (DataProfileItem item2 : dataProfiles) {
					if(!columnNames.contains(item2.getFieldName().toLowerCase()))continue;
					newExpression = appendDataProfileCondition(table, join.getOnExpression(), item2);
					join.setOnExpression(newExpression);
				}
				
			}
		}
		//
		String newSql = selectBody.toString();
		if(sqls.length == 2){
			newSql = sqls[1].replace(INNER_SQL_EXPR, newSql);
		}
		System.out.println("-----------------ORIGIN SQL-----------------------");
		System.out.println(originSql);
		System.out.println("-----------------NEW SQL-----------------------");
		System.out.println(newSql);
		
		return newSql;
	}
	
	private static Expression appendDataProfileCondition(Table table,Expression orginExpression,DataProfileItem item){
		Expression newExpression = null;
		Column column = new Column(table, item.getFieldName());
		if (item.getFieldValues().length == 1) {
			EqualsTo equalsTo = new EqualsTo();
			equalsTo.setLeftExpression(column);
			equalsTo.setRightExpression(new StringValue(item.getFieldValues()[0]));
			newExpression = orginExpression == null ? equalsTo : new AndExpression(orginExpression, equalsTo);
		} else {
			ExpressionList expressionList = new ExpressionList(new ArrayList<>(item.getFieldValues().length));
			for (String value : item.getFieldValues()) {
				expressionList.getExpressions().add(new StringValue(value));
			}
			InExpression inExpression = new InExpression(column, expressionList);
			newExpression = orginExpression == null ? inExpression : new AndExpression(orginExpression,inExpression);
		}
		
		return newExpression;
	}

}


