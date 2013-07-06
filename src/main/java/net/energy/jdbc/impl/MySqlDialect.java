package net.energy.jdbc.impl;

import net.energy.jdbc.Dialect;
import net.energy.utils.Assert;
import net.energy.utils.Page;

/**
 * 基于MySql的语法的SQL生成类
 * 
 * @author wuqh
 *
 */
public class MySqlDialect implements Dialect {

	@Override
	public String getPaginationSql(String sql, Page page) {
		return escapeLastSemicolon(sql) + " limit " + page.getStartIndex() + "," + page.getSize();
	}

	@Override
	public String getCountSql(String sql) {
		sql = escapeLastSemicolon(sql);
		return "select count(1) from (" + sql + ") cntTbl;";
	}

	String escapeLastSemicolon(String sql) {
		Assert.notNull(sql, "SQL不能为空");
		if (sql.endsWith(";")) {
			sql = sql.substring(0, sql.length() - 1);
		}
		return sql;
	}
}
