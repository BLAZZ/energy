package net.energy.jdbc.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 创建PreparedStatement后的回调方法接口
 * 
 * @author wuqh
 *
 * @param <T>
 */
public interface PreparedStatementCallback<T> {
	/**
	 * 在创建了PreparedStatement后调用此方法
	 * 
	 * @param ps
	 * @return
	 * @throws SQLException
	 */
	T doInPreparedStatement(PreparedStatement ps) throws SQLException;
}
