package net.energy.jdbc.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import net.energy.jdbc.RowMapper;
import net.energy.utils.JdbcUtils;

/**
 * 查询类SQL调用方法
 * 
 * @author wuqh
 *
 * @param <T>
 */
class QueryCallback<T> implements PreparedStatementCallback<List<T>> {
	private final RowMapper<T> rowMapper;
	
	public QueryCallback(RowMapper<T> rowMapper) {
		this.rowMapper = rowMapper;
	}
	
	@Override
	public List<T> doInPreparedStatement(PreparedStatement ps) throws SQLException {
		ResultSet rs = null;
		try {
			rs = ps.executeQuery();
			RowMapperResultSetExtractor<T> rse = new RowMapperResultSetExtractor<T>(rowMapper);
			return rse.extractData(rs);
		} finally {
			JdbcUtils.closeResultSet(rs);
		}
	}

}
