package net.energy.executor.jdbc;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.energy.definition.jdbc.JdbcBatchUpdateDefinition;
import net.energy.exception.DaoGenerateException;
import net.energy.exception.RetrievalIdException;
import net.energy.jdbc.JdbcDataAccessor;
import net.energy.jdbc.KeyHolder;
import net.energy.utils.ArrayHelper;
import net.energy.utils.ClassHelper;
import net.energy.utils.ReflectionUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JDBC的批量增删改操作调用方法
 * 
 * @author wuqh
 * 
 */
public class JdbcBatchUpdateExecutor extends AbstractJdbcExecutor {
	private static final Log LOGGER = LogFactory.getLog(JdbcBatchUpdateExecutor.class);
	private JdbcBatchUpdateDefinition definition;

	public JdbcBatchUpdateExecutor(JdbcDataAccessor dataAccessor, Method method) throws DaoGenerateException {
		super(dataAccessor, method);
	}

	@Override
	public Object execute(Object obj, Object[] args) {
		// 获取实际的PreparedSQL和调用参数
		String actualSql = definition.getActualSql(args);
		List<Object[]> paramArrays = ReflectionUtils.generateBatchQueryArguments(args, definition);

		KeyHolder keyHolder = null;
		if (definition.isReturnId()) {
			keyHolder = dataAccessor.getKeyHolder();
		}

		LOGGER.info("批量更新操作执行SQL[" + actualSql + "]");
		int[] rows = dataAccessor.batchUpdate(actualSql, paramArrays, keyHolder);

		if (keyHolder != null) {
			List<Number> keys = getKeysFromKeyHolder(keyHolder);
			return formatKeys(keys, definition);
		} else {
			return rows;
		}
	}

	@Override
	protected void initDefinition(Method method) throws DaoGenerateException {
		definition = new JdbcBatchUpdateDefinition(method);
	}

	/**
	 * 插入多行数据时，且主键为单一主键。调用此方法用于获取这些插入行的主键。
	 * 
	 * @param keyHolder
	 * @return
	 */
	private List<Number> getKeysFromKeyHolder(KeyHolder keyHolder) {
		List<Map<String, Object>> generatedKeys = keyHolder.getKeyList();
		int length = generatedKeys.size();

		List<Number> keys = new ArrayList<Number>();
		for (int i = 0; i < length; i++) {
			Iterator<Object> keyIter = generatedKeys.get(i).values().iterator();
			if (keyIter.hasNext()) {
				Object key = keyIter.next();
				if (!(key instanceof Number)) {
					String className = null;
					if (key != null) {
						className = key.getClass().getName();
					}
					throw new RetrievalIdException("数据库生成的主键不是系统支持的数值类型. " + "无法将[" + className + "]类型转换为["
							+ Number.class.getName() + "]类型");
				}
				keys.add((Number) key);
			}
		}

		return keys;
	}

	/**
	 * 将返回的Key值格式化成所需要的返回值格式：数组或者List
	 * 
	 * @param keys
	 * @param batchUpdateDefinition
	 * @return
	 */
	private Object formatKeys(List<Number> keys, JdbcBatchUpdateDefinition batchUpdateDefinition) {
		if (batchUpdateDefinition.isReturnList()) {
			return keys;
		}

		int length = keys.size();
		// 将返回结果从List<? extends Numer>转换为数组
		Class<?> componentType = batchUpdateDefinition.getReturnComponentType();
		if (!ClassHelper.isTypePrimitive(componentType)) {
			Object array = Array.newInstance(componentType, length);
			System.arraycopy(keys.toArray(), 0, array, 0, length);
			return array;
		}

		return formatPrimitiveKeys(keys, componentType, length);

	}

	/**
	 * 将List<? extends Numer>转换为基本类型数组
	 * 
	 * @param keys
	 * @param componentType
	 * @param length
	 * @return
	 */
	private Object formatPrimitiveKeys(List<Number> keys, Class<?> componentType, int length) {
		if (long.class.equals(componentType)) {
			Long[] array = keys.toArray(new Long[length]);
			return ArrayHelper.toPrimitive(array);
		}
		if (int.class.equals(componentType)) {
			Integer[] array = keys.toArray(new Integer[length]);
			return ArrayHelper.toPrimitive(array);
		}
		if (boolean.class.equals(componentType)) {
			Boolean[] array = keys.toArray(new Boolean[length]);
			return ArrayHelper.toPrimitive(array);
		}
		if (double.class.equals(componentType)) {
			Double[] array = keys.toArray(new Double[length]);
			return ArrayHelper.toPrimitive(array);
		}
		if (char.class.equals(componentType)) {
			Character[] array = keys.toArray(new Character[length]);
			return ArrayHelper.toPrimitive(array);
		}
		if (float.class.equals(componentType)) {
			Float[] array = keys.toArray(new Float[length]);
			return ArrayHelper.toPrimitive(array);
		}
		if (byte.class.equals(componentType)) {
			Byte[] array = keys.toArray(new Byte[length]);
			return ArrayHelper.toPrimitive(array);
		}
		if (short.class.equals(componentType)) {
			Short[] array = keys.toArray(new Short[length]);
			return ArrayHelper.toPrimitive(array);
		}
		throw new RetrievalIdException("无法将返回值转换为类型为[" + componentType + "的数组");
	}
}
