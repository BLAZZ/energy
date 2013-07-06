package net.energy.jdbc.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import net.energy.jdbc.KeyHolder;
import net.energy.utils.JdbcUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 增删改类SQL调用方法
 * 
 * @author wuqh
 */
class UpdateCallback implements PreparedStatementCallback<Integer> {
	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateCallback.class);

	private final KeyHolder generatedKeyHolder;

	public UpdateCallback(KeyHolder generatedKeyHolder) {
		this.generatedKeyHolder = generatedKeyHolder;
	}

	@Override
	public Integer doInPreparedStatement(PreparedStatement ps) throws SQLException {
		// 执行PreparedStatement
		int rows = ps.executeUpdate();
		// 根据是否存在KeyHolder判断是否需要从PreparedStatement中提取生成的主键
		if (generatedKeyHolder != null) {
			List<Map<String, Object>> generatedKeys = generatedKeyHolder.getKeyList();
			generatedKeys.clear();
			ResultSet keys = ps.getGeneratedKeys();
			if (keys != null) {
				try {
					RowMapperResultSetExtractor<Map<String, Object>> rse = new RowMapperResultSetExtractor<Map<String, Object>>(
							new ColumnMapRowMapper(), 1);
					generatedKeys.addAll(rse.extractData(keys));
				} finally {
					JdbcUtils.closeResultSet(keys);
				}
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("SQL更新操作更新了[" + rows + "]条记录，返回了[" + generatedKeys.size() + "]条主键");
			}
		}

		return rows;
	}

}
