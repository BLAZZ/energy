package net.energy.executor.jdbc;

import java.lang.reflect.Method;
import java.util.List;

import net.energy.definition.jdbc.JdbcUpdateDefinition;
import net.energy.exception.DaoGenerateException;
import net.energy.jdbc.JdbcDataAccessor;
import net.energy.jdbc.KeyHolder;
import net.energy.utils.ReflectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JDBC的增删改操作调用方法
 * 
 * @author wuqh
 * 
 */
public class JdbcUpdateExecutor extends AbstractJdbcExecutor {
	private static final Logger LOGGER = LoggerFactory.getLogger(JdbcUpdateExecutor.class);
	private JdbcUpdateDefinition definition;

	public JdbcUpdateExecutor(JdbcDataAccessor dataAccessor, Method method) throws DaoGenerateException {
		super(dataAccessor, method);
	}

	@Override
	public Object execute(Object obj, Object[] args) {
		Method[] getterMethods = definition.getGetterMethods();
		Integer[] parameterIndexes = definition.getParameterIndexes();
		boolean isReturnId = definition.isReturnId();
		// 获取实际用于执行的preparedSQL
		String actualSql = definition.getActualSql(args);
		List<String> parameterNames = definition.getParsedSql().getParameterNames();
		Object[] paramArray = ReflectionUtils.fetchValues(getterMethods, parameterIndexes, args, parameterNames);

		KeyHolder keyHolder = null;
		if (isReturnId) {
			keyHolder = dataAccessor.getKeyHolder();
		}

		LOGGER.info("更新操作执行SQL[" + actualSql + "]");
		int rows = dataAccessor.update(actualSql, keyHolder, paramArray);

		if (keyHolder != null) {
			return keyHolder.getKey();
		} else {
			return rows;
		}
	}

	@Override
	protected void initDefinition(Method method) throws DaoGenerateException {
		definition = new JdbcUpdateDefinition(method);
	}

}
