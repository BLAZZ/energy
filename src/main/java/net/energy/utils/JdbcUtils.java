package net.energy.utils;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;

import javax.sql.DataSource;

import net.energy.exception.JdbcDataAccessException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JDBC操作工具类
 * 
 * @author wuqh
 * 
 */
public class JdbcUtils {
	private static final Log LOGGER = LogFactory.getLog(JdbcUtils.class);

	/**
	 * 从DataSource中获取JDBC Connection
	 * 
	 * @param dataSource
	 * @return
	 */
	public static Connection getConnection(DataSource dataSource) {
		try {
			Assert.notNull(dataSource, "必须指定DataSource");

			return dataSource.getConnection();
		} catch (SQLException ex) {
			throw new JdbcDataAccessException("获取JDBC Connection失败", ex);
		}
	}

	/**
	 * 创建PreparedStatement
	 * 
	 * @param con
	 *            JDBC Connection
	 * @param sql
	 *            PreparedSQL
	 * @param returnKeys
	 *            是否returnId
	 * @param args
	 *            PreparedSQL对应的参数值
	 * @return
	 * @throws SQLException
	 */
	public static PreparedStatement createPreparedStatement(final Connection con, final String sql,
			final boolean returnKeys, final Object... args) throws SQLException {
		PreparedStatement ps = null;
		if (returnKeys) {
			ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		} else {
			ps = con.prepareStatement(sql);
		}

		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				Object arg = args[i];
				setParameterValue(ps, i + 1, arg);
			}
		}

		return ps;
	}

	/**
	 * 设置PreparedSQL对应的参数值
	 * 
	 * @param ps
	 * @param paramIndex
	 * @param inValue
	 * @throws SQLException
	 */
	public static void setParameterValue(PreparedStatement ps, int paramIndex, Object inValue) throws SQLException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("绑定PreparedStatement参数: 参数列序号[" + paramIndex + "], 参数值["
					+ inValue + "],参数类型[" + (inValue != null ? inValue.getClass().getName() : "null") + "]");
		}

		if (inValue == null) {
			setNull(ps, paramIndex);
		} else {
			setValue(ps, paramIndex, inValue);
		}
	}

	/**
	 * 设置PreparedSQL对应的参数值为null。需要通过判断数据库类型判断需要设置的Types
	 * 
	 * @param ps
	 * @param paramIndex
	 * @param inValue
	 * @throws SQLException
	 */
	private static void setNull(PreparedStatement ps, int paramIndex) throws SQLException {
		boolean useSetObject = false;
		int sqlType = Types.NULL;
		try {
			DatabaseMetaData dbmd = ps.getConnection().getMetaData();
			String databaseProductName = dbmd.getDatabaseProductName();
			String jdbcDriverName = dbmd.getDriverName();
			if (databaseProductName.startsWith("Informix") || jdbcDriverName.startsWith("Microsoft SQL Server")) {
				useSetObject = true;
			} else if (databaseProductName.startsWith("DB2") || jdbcDriverName.startsWith("jConnect")
					|| jdbcDriverName.startsWith("SQLServer") || jdbcDriverName.startsWith("Apache Derby")) {
				sqlType = Types.VARCHAR;
			}
		} catch (Throwable ex) {
			LOGGER.debug("获取JDBC驱动信息失败", ex);
		}
		if (useSetObject) {
			ps.setObject(paramIndex, null);
		} else {
			ps.setNull(paramIndex, sqlType);
		}
	}

	/**
	 * 设置PreparedSQL对应的参数值。对于日期类型需要做特殊处理
	 * 
	 * @param ps
	 * @param paramIndex
	 * @param inValue
	 * @throws SQLException
	 */
	private static void setValue(PreparedStatement ps, int paramIndex, Object inValue) throws SQLException {
		if (EnergyClassUtils.isTypeString(inValue.getClass())) {
			ps.setString(paramIndex, inValue.toString());
		} else if (EnergyClassUtils.isTypeDate(inValue.getClass())) {
			ps.setTimestamp(paramIndex, new Timestamp(((java.util.Date) inValue).getTime()));
		} else if (inValue instanceof Calendar) {
			Calendar cal = (Calendar) inValue;
			ps.setTimestamp(paramIndex, new Timestamp(cal.getTime().getTime()), cal);
		} else {
			// Fall back to generic setObject call without SQL type specified.
			ps.setObject(paramIndex, inValue);
		}
	}

	/**
	 * 关闭JDBC Connection，忽略关闭过程中产生的Exception。
	 * 
	 * @param con
	 */
	public static void closeConnection(Connection con) {
		if (con != null) {
			try {
				if (!con.isClosed()) { // 关闭没有关闭
					con.close();
				}
			} catch (SQLException ex) {
				LOGGER.debug("关闭JDBC Connection失败", ex);
			} catch (Throwable ex) {
				// We don't trust the JDBC driver: It might throw
				// RuntimeException or Error.
				LOGGER.debug("关闭JDBC Connection时发生未知异常", ex);
			}
		}
	}

	/**
	 * 关闭JDBC Statement，忽略关闭过程中产生的Exception。
	 * 
	 * @param stmt
	 */
	public static void closeStatement(Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException ex) {
				LOGGER.trace("关闭JDBC Statement失败", ex);
			} catch (Throwable ex) {
				// We don't trust the JDBC driver: It might throw
				// RuntimeException or Error.
				LOGGER.trace("关闭JDBC Statement时发生未知异常", ex);
			}
		}
	}

	/**
	 * 关闭JDBC ResultSet，忽略关闭过程中产生的Exception。
	 * 
	 * @param rs
	 */
	public static void closeResultSet(ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException ex) {
				LOGGER.trace("关闭JDBC ResultSet失败", ex);
			} catch (Throwable ex) {
				// We don't trust the JDBC driver: It might throw
				// RuntimeException or Error.
				LOGGER.trace("关闭JDBC ResultSet发生未知异常", ex);
			}
		}
	}

	/**
	 * 判断JDBC驱动是否支持JDBC 2.0的批量更新。 在执行statements前调用一次，用来判断SQL
	 * statements是否可以采用JDBC 2.0的batch机制执行来时需要用采用传统的逐一执行的方式。
	 * 
	 * @param con
	 * @return
	 */
	public static boolean supportsBatchUpdates(Connection con) {
		try {
			DatabaseMetaData dbmd = con.getMetaData();
			if (dbmd != null) {
				if (dbmd.supportsBatchUpdates()) {
					LOGGER.debug("JDBC驱动支持批量更新");
					return true;
				} else {
					LOGGER.debug("JDBC驱动不支持批量更新");
				}
			}
		} catch (SQLException ex) {
			LOGGER.debug("调用JDBC驱动'supportsBatchUpdates'方法发生异常", ex);
		} catch (AbstractMethodError err) {
			LOGGER.debug("JDBC不支持JDBC 2.0的'supportsBatchUpdates'方法", err);
		}
		return false;
	}

	/**
	 * 释放连接
	 * 
	 * @param con
	 */
	public static void releaseConnection(Connection con) {
		if (con != null) {
			closeConnection(con);
		}
	}

	/**
	 * 获取数据库第columnIndex个字段的名字
	 * 
	 * @param resultSetMetaData
	 * @param columnIndex
	 * @return
	 * @throws SQLException
	 */
	public static String lookupColumnName(ResultSetMetaData resultSetMetaData, int columnIndex) throws SQLException {
		String name = resultSetMetaData.getColumnLabel(columnIndex);
		if (name == null || name.length() < 1) {
			name = resultSetMetaData.getColumnName(columnIndex);
		}
		return name;
	}

	/**
	 * 提取ResultSet第index个字段的值。默认采用ResultSet的getObject方法。
	 * 另外，通过对于Blob会转换为byte[]，对于Clob转换为String。并对oracle非标准TIMESTAMP进行特殊处理。
	 * 
	 * @param rs
	 * @param index
	 * @return
	 * @throws SQLException
	 */
	public static Object getResultSetValue(ResultSet rs, int index) throws SQLException {
		Object obj = rs.getObject(index);
		String className = null;
		if (obj != null) {
			className = obj.getClass().getName();
		}
		if (obj instanceof Blob) {
			obj = rs.getBytes(index);
		} else if (obj instanceof Clob) {
			obj = rs.getString(index);
		} else if (className != null
				&& ("oracle.sql.TIMESTAMP".equals(className) || "oracle.sql.TIMESTAMPTZ".equals(className))) {
			obj = rs.getTimestamp(index);
		} else if (className != null && className.startsWith("oracle.sql.DATE")) {
			String metaDataClassName = rs.getMetaData().getColumnClassName(index);
			if ("java.sql.Timestamp".equals(metaDataClassName) || "oracle.sql.TIMESTAMP".equals(metaDataClassName)) {
				obj = rs.getTimestamp(index);
			} else {
				obj = rs.getDate(index);
			}
		} else if (obj != null && obj instanceof java.sql.Date) {
			if ("java.sql.Timestamp".equals(rs.getMetaData().getColumnClassName(index))) {
				obj = rs.getTimestamp(index);
			}
		}
		return obj;
	}

}
