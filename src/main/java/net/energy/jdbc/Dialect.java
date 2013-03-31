package net.energy.jdbc;

import net.energy.utils.Page;

/**
 * SQL语法接口，用于生产适应不同数据库的分页和统计总数的SQL
 * 
 * @author wuqh
 * 
 */
public interface Dialect {
	/**
	 * 根据查询的SQL生成统计总记录数的SQL
	 * 
	 * @param sql
	 * @return
	 */
	String getCountSql(String sql);

	/**
	 * 根据查询的SQL和分页信息生成分页的SQL
	 * 
	 * @param sql
	 * @param page
	 * @return
	 */
	String getPaginationSql(String sql, Page page);
}
