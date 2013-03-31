package net.energy.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 和SpringJdbcTemplate中使用的RowMapper一样的接口
 * 
 * @author wuqh
 *
 * @param <T>
 */
public interface RowMapper<T> {
	/**
	 * ORM映射方法
	 * 
	 * @param rs
	 * @param rowNum
	 * @return
	 * @throws SQLException
	 */
	T mapRow(ResultSet rs, int rowNum) throws SQLException;

}
