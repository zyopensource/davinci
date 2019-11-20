package test;

import java.util.ArrayList;
import java.util.List;

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
		
		List<UserDataProfileItem> dataProfiles = new ArrayList<UserDataProfileItem>();
		UserDataProfileItem profileItem = new UserDataProfileItem();
		profileItem.setName("app_id");
		profileItem.setValues(new String[]{"1001"});
		dataProfiles.add(profileItem);
		
		profileItem = new UserDataProfileItem();
		profileItem.setName("app_id");
		dataProfiles.add(profileItem);

		UserDataProfileContextHolder.set(dataProfiles);
		
		String sql = "SELECT  `account_id` FROM (SELECT log.* FROM balance_trade_logs log) T GROUP BY `account_id`";
		
		String profileSql = SqlExtUtils.rebuildSqlWithUserDataProfile(null,sql);
		System.out.println(profileSql);
		
		
		
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

	
	
}
