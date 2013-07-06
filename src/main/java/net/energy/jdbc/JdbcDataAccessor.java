package net.energy.jdbc;

import java.util.List;

import net.energy.utils.Page;

/**
 * JDBC数据访问接口
 * 
 * @author wuqh
 * 
 */
public interface JdbcDataAccessor {
	/**
	 * 查询
	 * 
	 * @param <T>
	 * @param sql
	 * @param rowMapper
	 * @param fetchSize
	 * @param args
	 * @return
	 */
	<T> List<T> query(String sql, RowMapper<T> rowMapper, int fetchSize, Object... args);

	/**
	 * 增删改
	 * 
	 * @param sql
	 * @param generatedKeyHolder
	 * @param args
	 * @return
	 */
	int update(String sql, KeyHolder generatedKeyHolder, Object... args);

	/**
	 * 批量的增删改
	 * 
	 * @param sql
	 * @param argsList
	 * @param generatedKeyHolder
	 * @return
	 */
	int[] batchUpdate(String sql, List<Object[]> argsList, KeyHolder generatedKeyHolder);

	/**
	 * 分页查询
	 * 
	 * @param <T>
	 * @param sql
	 * @param page
	 * @param rowMapper
	 * @param fetchSize
	 * @param args
	 * @return
	 */
	<T> List<T> queryPage(String sql, Page page, RowMapper<T> rowMapper, int fetchSize, Object... args);

	/**
	 * 获取KeyHolder实例，在@ReturnId时使用
	 * 
	 * @return
	 */
	KeyHolder getKeyHolder();
}
