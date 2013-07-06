package net.energy.jdbc.impl;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.energy.exception.DaoGenerateException;
import net.energy.exception.DataAccessException;
import net.energy.jdbc.RowMapper;
import net.energy.utils.BeanAutoDetect;
import net.energy.utils.ClassHelper;
import net.sf.cglib.core.ReflectUtils;

public class AutoDetectRowMapper<T> extends BeanAutoDetect implements RowMapper<T> {
	private final Set<String> columnNameSet = new HashSet<String>();
	private final Map<String, String> columnClassName = new HashMap<String, String>();

	public AutoDetectRowMapper(Class<T> clazz) throws DaoGenerateException {
		super(clazz);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T mapRow(ResultSet rs, int rowNum) throws SQLException {
		T result = (T) ReflectUtils.newInstance(clazz);

		if (result == null) {
			return result;
		}

		initColumnName(rs);

		for (String property : columnNameSet) {
			Method writeMethod = writeMethods.get(property);
			Object object = getResultSetValue(rs, property);

			try {
				writeMethod.invoke(writeMethod, convert(object, property));
			} catch (Exception e) {
				throw new DataAccessException("映射数据失败:", e);
			}
		}

		return null;
	}

	private void initColumnName(ResultSet rs) throws SQLException {
		if (columnNameSet.isEmpty()) {
			synchronized (columnNameSet) {
				if (columnNameSet.isEmpty()) {
					ResultSetMetaData metaData = rs.getMetaData();
					int columnCount = metaData.getColumnCount();
					for (int i = 1; i <= columnCount; i++) {
						columnNameSet.add(metaData.getColumnName(i));
						columnClassName.put(metaData.getColumnName(i), metaData.getColumnClassName(i));
					}
				}
			}
		}

	}

	private Object getResultSetValue(ResultSet rs, String columnName) throws SQLException {
		Object obj = rs.getObject(columnName);
		String className = null;
		if (obj != null) {
			className = obj.getClass().getName();
		}
		if (obj instanceof Blob) {
			obj = rs.getBytes(columnName);
		} else if (obj instanceof Clob) {
			obj = rs.getString(columnName);
		} else if (className != null
				&& ("oracle.sql.TIMESTAMP".equals(className) || "oracle.sql.TIMESTAMPTZ".equals(className))) {
			obj = rs.getTimestamp(columnName);
		} else if (className != null && className.startsWith("oracle.sql.DATE")) {
			String metaDataClassName = columnClassName.get(columnName);
			if ("java.sql.Timestamp".equals(metaDataClassName) || "oracle.sql.TIMESTAMP".equals(metaDataClassName)) {
				obj = rs.getTimestamp(columnName);
			} else {
				obj = rs.getDate(columnName);
			}
		} else if (obj != null && obj instanceof java.sql.Date) {
			if ("java.sql.Timestamp".equals(columnClassName.get(columnName))) {
				obj = rs.getTimestamp(columnName);
			}
		}
		return obj;
	}

	@Override
	public boolean isAllowAutoMapType(Class<?> clazz) {
		return clazz.isPrimitive() || ClassHelper.wrapperToPrimitive(clazz) != null || clazz.equals(String.class)
				|| clazz.isEnum() || clazz.equals(BigDecimal.class) || clazz.equals(Number.class)
				|| clazz.equals(BigDecimal.class) || Date.class.isAssignableFrom(clazz) || clazz.equals(byte[].class);
	}
}
