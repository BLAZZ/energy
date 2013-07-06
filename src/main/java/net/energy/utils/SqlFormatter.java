package net.energy.utils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * 用于替换SQL中的通用表名的工具类
 * 
 * @author wuqh
 * 
 */
public final class SqlFormatter {
	private static final Map<String, String> formattedSql = new HashMap<String, String>();

	private SqlFormatter() {
	}

	/**
	 * 按照MessageFormat的规则替换SQL中的通用表名
	 * 
	 * @param sqlNeedFormat
	 * @param tableNames
	 * @return
	 */
	public static String formatSql(String sqlNeedFormat, String[] tableNames) {
		StringBuilder key = new StringBuilder();
		for (String tableName : tableNames) {
			key.append(tableName);
		}
		key.append(sqlNeedFormat);
		String keyStr = key.toString();
		String sql = formattedSql.get(keyStr);
		if (sql == null) {
			MessageFormat format = new MessageFormat(sqlNeedFormat);
			sql = format.format(tableNames);
			formattedSql.put(keyStr, sql);
		}
		return sql;

	}
}
