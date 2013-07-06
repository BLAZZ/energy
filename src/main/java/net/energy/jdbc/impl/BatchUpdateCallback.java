package net.energy.jdbc.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.energy.jdbc.KeyHolder;
import net.energy.utils.JdbcUtils;

/**
 * 批量增删改类SQL调用方法
 * 
 * @author wuqh
 * 
 *
 */
class BatchUpdateCallback implements PreparedStatementCallback<int[]> {
	private final KeyHolder generatedKeyHolder;
	private final List<Object[]> argsList;

	public BatchUpdateCallback(KeyHolder generatedKeyHolder, List<Object[]> argsList) {
		this.generatedKeyHolder = generatedKeyHolder;
		this.argsList = argsList;
	}

	@Override
	public int[] doInPreparedStatement(PreparedStatement ps) throws SQLException {
		int batchSize = argsList.size();
		// 判断JDBC是否支持批量更新，如果支持直接调用批量执行方法，否则逐行执行。
		if (JdbcUtils.supportsBatchUpdates(ps.getConnection())) {
			for (int i = 0; i < batchSize; i++) {
				setParameterValue(ps, argsList, i);
				ps.addBatch();
			}
			int[] rows = ps.executeBatch();
			return processBatchResult(ps, rows, true, generatedKeyHolder);
		} else {
			List<Integer> rowsAffected = new ArrayList<Integer>();
			int[] rows = new int[1];
			for (int i = 0; i < batchSize; i++) {
				setParameterValue(ps, argsList, i);
				int rowAffected = ps.executeUpdate();
				rows[0] = rowAffected;
				rowsAffected.add(rowAffected);
				processBatchResult(ps, rows, (i == 0), generatedKeyHolder); // 第一次清空KeyHolder
			}
			return processAffectedArray(rowsAffected);
		}
	}

	/**
	 * 对于不支持批量更新的JDBC，需要List<Integer>将转换为int[]
	 * 
	 * @param rowsAffected
	 *            每条更新记录的结果
	 * @return
	 */
	private int[] processAffectedArray(List<Integer> rowsAffected) {
		int[] rowsAffectedArray = new int[rowsAffected.size()];
		for (int i = 0; i < rowsAffectedArray.length; i++) {
			rowsAffectedArray[i] = rowsAffected.get(i);
		}
		return rowsAffectedArray;
	}

	/**
	 * 处理批量执行的结果：根据是否存在KeyHolder判断是否需要从PreparedStatement中提取返回的主键。
	 * 
	 * @param ps
	 * @param rows
	 * @param clearKeys
	 * @param generatedKeyHolder
	 * @return
	 * @throws SQLException
	 */
	private int[] processBatchResult(PreparedStatement ps, int[] rows, boolean clearKeys,
			final KeyHolder generatedKeyHolder) throws SQLException {
		if (generatedKeyHolder != null) {
			List<Map<String, Object>> generatedKeys = generatedKeyHolder.getKeyList();
			// 如果clearKeys为true，表示为第一次调用，需要清除generatedKeys中的原有值。
			if (clearKeys) {
				generatedKeys.clear();
			}
			ResultSet keys = ps.getGeneratedKeys();
			if (keys != null) {
				try {
					RowMapperResultSetExtractor<Map<String, Object>> rse = new RowMapperResultSetExtractor<Map<String, Object>>(
							new ColumnMapRowMapper(), rows.length);
					generatedKeys.addAll(rse.extractData(keys));
				} finally {
					JdbcUtils.closeResultSet(keys);
				}
			}
		}
		return rows;
	}

	/**
	 * 设置PreparedSQL对应的参数值
	 * 
	 * @param ps
	 * @param argsList
	 * @param idx
	 * @throws SQLException
	 */
	private void setParameterValue(final PreparedStatement ps, final List<Object[]> argsList, int idx)
			throws SQLException {
		Object[] args = argsList.get(idx);
		if (args != null) {
			for (int index = 0; index < args.length; index++) {
				Object arg = args[index];
				JdbcUtils.setParameterValue(ps, index + 1, arg);
			}
		}
	}
}
