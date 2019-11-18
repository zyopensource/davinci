package test;

import java.util.ArrayList;
import java.util.List;

import edp.core.utils.SqlExtUtils;
import edp.davinci.addons.UserDataProfileItem;
import edp.davinci.addons.UserDataProfileContextHolder;
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
		dataProfiles.add(new UserDataProfileItem("account_id", new String[]{"1001","10002"}));
		dataProfiles.add(new UserDataProfileItem("app_id", new String[]{"1001"}));
		dataProfiles.add(new UserDataProfileItem("seller_id", new String[]{"9999"}));
		
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
