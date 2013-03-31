package net.energy.jdbc.impl;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import net.energy.jdbc.RowMapper;
import net.energy.utils.JdbcUtils;

/**
 * 将ResultSet映射成Map，key为column，value为column对应的值
 * 
 * @author wuqh
 * 
 */
class ColumnMapRowMapper implements RowMapper<Map<String, Object>> {

	public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		Map<String, Object> mapOfColValues = createColumnMap(columnCount);
		for (int i = 1; i <= columnCount; i++) {
			String key = getColumnKey(JdbcUtils.lookupColumnName(rsmd, i));
			Object obj = getColumnValue(rs, i);
			mapOfColValues.put(key, obj);
		}
		return mapOfColValues;
	}

	/**
	 * 创建字段映射Map实例
	 * 
	 * @param columnCount
	 * @return
	 */
	protected Map<String, Object> createColumnMap(int columnCount) {
		return new LinkedHashMap<String, Object>(columnCount);
	}

	/**
	 * 根据columnName获取字段映射Map的key值
	 * 
	 * @param columnName
	 * @return
	 */
	protected String getColumnKey(String columnName) {
		return columnName;
	}

	/**
	 * 提取ResultSet第index个字段的值。默认采用ResultSet的getObject方法。另外，通过对于Blob会转换为byte[]，
	 * 对于Clob转换为String。并对oracle非标准TIMESTAMP进行特殊处理。
	 * 
	 * @param rs
	 * @param index
	 * @return
	 * @throws SQLException
	 */
	protected Object getColumnValue(ResultSet rs, int index) throws SQLException {
		return JdbcUtils.getResultSetValue(rs, index);
	}

}