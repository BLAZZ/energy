package net.energy.jdbc.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import net.energy.exception.JdbcDataAccessException;
import net.energy.jdbc.Dialect;
import net.energy.jdbc.JdbcDataAccessor;
import net.energy.jdbc.KeyHolder;
import net.energy.jdbc.RowMapper;
import net.energy.utils.JdbcUtils;
import net.energy.utils.Page;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JdbcDataAccessor的默认实现
 * 
 * @author wuqh
 * 
 */
public class SimpleJdbcDataAccessor implements JdbcDataAccessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleJdbcDataAccessor.class);

	private DataSource dataSource;
	private Dialect dialect;

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setDialect(Dialect dialect) {
		this.dialect = dialect;
	}

	/**
	 * SQL执行模板
	 * 
	 * 
	 * @param <T>
	 * @param sql
	 * @param fetchSize
	 * @param returnKeys
	 * @param action
	 * @param args
	 * @return
	 */
	protected <T> T execute(String sql, int fetchSize, boolean returnKeys, PreparedStatementCallback<T> action,
			Object... args) {
		// 创建Connection
		Connection con = JdbcUtils.getConnection(dataSource);
		PreparedStatement ps = null;
		try {
			// 创建PreparedStatement
			ps = JdbcUtils.createPreparedStatement(con, sql, returnKeys, args);

			if (fetchSize > 0) {
				ps.setFetchSize(fetchSize);
			}
			// 执行PreparedStatement
			T result = action.doInPreparedStatement(ps);
			return result;
		} catch (SQLException ex) {
			JdbcUtils.closeStatement(ps);
			ps = null;
			JdbcUtils.releaseConnection(con);
			con = null;
			throw new JdbcDataAccessException(ex);
		} finally {
			JdbcUtils.closeStatement(ps);
			JdbcUtils.releaseConnection(con);
		}
	}

	@Override
	public <T> List<T> query(String sql, RowMapper<T> rowMapper, int fetchSize, Object... args) {
		QueryCallback<T> qc = new QueryCallback<T>(rowMapper);
		return execute(sql, fetchSize, false, qc, args);
	}

	@Override
	public int update(String sql, KeyHolder generatedKeyHolder, Object... args) {
		UpdateCallback uc = new UpdateCallback(generatedKeyHolder);
		boolean returnKeys = (generatedKeyHolder != null);
		return execute(sql, 0, returnKeys, uc, args);
	}

	@Override
	public int[] batchUpdate(String sql, List<Object[]> argsList, KeyHolder generatedKeyHolder) {
		BatchUpdateCallback buc = new BatchUpdateCallback(generatedKeyHolder, argsList);
		boolean returnKeys = (generatedKeyHolder != null);
		return execute(sql, 0, returnKeys, buc);
	}

	@Override
	public int queryCount(String sql, Object... args) {
		String countSql = dialect.getCountSql(sql);

		LOGGER.info("用于Count操作的SQL:" + countSql);
		RowMapper<Integer> rowMapper = new CountRowMapper();
		List<Integer> results = query(countSql, rowMapper, 0, args);
		return getSingleResult(results);
	}

	@Override
	public <T> List<T> queryPage(String sql, Page page, RowMapper<T> rowMapper, int fetchSize, Object... args) {
		int total = queryCount(sql, args);

		page.setTotal(total);
		if (total <= 0) {
			return new ArrayList<T>(0);
		}
		String pageSql = dialect.getPaginationSql(sql, page);

		LOGGER.info("分页查询操作SQL:" + pageSql);

		return query(pageSql, rowMapper, fetchSize, args);
	}

	@Override
	public KeyHolder getKeyHolder() {
		return new GeneratedKeyHolder();
	}

	/**
	 * 返回单条记录
	 * 
	 * @param <T>
	 * @param results
	 * @return
	 */
	public static <T> T getSingleResult(Collection<T> results) {
		int size = (results != null ? results.size() : 0);
		if (size == 0) {
			return null;
		}
		return results.iterator().next();
	}
}
