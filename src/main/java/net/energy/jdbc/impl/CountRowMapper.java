package net.energy.jdbc.impl;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import net.energy.exception.CountRowNumberException;
import net.energy.jdbc.RowMapper;

/**
 * 用于将count操作的结果转换为Integer的RowMapper
 * 
 * @author wuqh
 * 
 */
class CountRowMapper implements RowMapper<Integer> {
	public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
		// Validate column count.
		ResultSetMetaData rsmd = rs.getMetaData();
		int nrOfColumns = rsmd.getColumnCount();
		if (nrOfColumns != 1) {
			throw new CountRowNumberException("Incorrect column count: expected 1, actual " + nrOfColumns);
		}
		return rs.getInt(1);
	}

}
