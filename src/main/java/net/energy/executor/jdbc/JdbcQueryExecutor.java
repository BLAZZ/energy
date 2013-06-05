package net.energy.executor.jdbc;

import java.lang.reflect.Method;
import java.util.List;

import net.energy.definition.jdbc.JdbcQueryDefinition;
import net.energy.exception.DaoGenerateException;
import net.energy.jdbc.JdbcDataAccessor;
import net.energy.jdbc.RowMapper;
import net.energy.utils.Page;
import net.energy.utils.ReflectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JDBC的查询操作调用方法
 * 
 * @author wuqh
 * 
 */
public class JdbcQueryExecutor extends AbstractJdbcExecutor {
	private static final Logger LOGGER = LoggerFactory.getLogger(JdbcQueryExecutor.class);
	private JdbcQueryDefinition definition;

	public JdbcQueryExecutor(JdbcDataAccessor dataAccessor, Method method) throws DaoGenerateException {
		super(dataAccessor, method);
	}

	@Override
	public Object execute(Object obj, Object[] args) {
		Method[] getterMethods = definition.getGetterMethods();
		Integer[] parameterIndexes = definition.getParameterIndexes();
		RowMapper<?> rowMapper = definition.getRowMapper();
		int fetchSize = definition.getFetchSize();
		// 获取实际用于执行的preparedSQL
		String actualSql = definition.getActualSql(args);
		List<String> parameterNames = definition.getParsedSql().getParameterNames();
		Object[] paramArray = ReflectionUtils.fetchVlaues(getterMethods, parameterIndexes, args, parameterNames);
		// 判断是否为分页查询，如果是分页查询就按分页查询的方式处理
		Page page = definition.getPageArgument(args);
		if (page != null) {
			return dataAccessor.queryPage(actualSql, page, rowMapper, fetchSize, paramArray);
		}
		// 非分页查询按普通查询方式处理
		LOGGER.info("查询操作执行SQL[" + actualSql + "]");

		List<?> result = dataAccessor.query(actualSql, rowMapper, fetchSize, paramArray);
		if (definition.isUnique()) {
			if (result == null || result.isEmpty()) {
				return null;
			}
			if (result.size() > 1) {
				LOGGER.debug("返回记录数 >1,默认获取第一条记录");
			}
			return result.get(0);
		} else {
			return result;
		}
	}

	@Override
	protected void initDefinition(Method method) throws DaoGenerateException {
		definition = new JdbcQueryDefinition(method);
	}

}
