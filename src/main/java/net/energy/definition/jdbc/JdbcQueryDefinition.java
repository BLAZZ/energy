package net.energy.definition.jdbc;

import java.lang.reflect.Method;
import java.util.Map;

import net.energy.annotation.Unique;
import net.energy.annotation.jdbc.MapperBy;
import net.energy.annotation.jdbc.Query;
import net.energy.exception.DaoGenerateException;
import net.energy.jdbc.RowMapper;
import net.energy.jdbc.impl.AutoDetectRowMapper;
import net.energy.utils.ClassHelper;
import net.sf.cglib.core.ReflectUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 通过对配置了@Query的方法的解析，产生需要在执行JDBC操作时必要用到的参数。
 * 
 * @author wuqh
 * 
 */
public class JdbcQueryDefinition extends BaseJdbcDefinition {
	private static final Log LOGGER = LogFactory.getLog(JdbcQueryDefinition.class);
	private int fetchSize;
	private boolean isUnique = false;
	private RowMapper<?> rowMapper;

	public JdbcQueryDefinition(Method method) throws DaoGenerateException {
		super(method);
	}

	@Override
	protected String getSourceSql(Method method) {
		Query query = method.getAnnotation(Query.class);
		return query.value();
	}

	@Override
	protected void parseInternal(Method method, Map<String, Integer> paramIndexes,
			Map<String, Integer> batchParamIndexes) throws DaoGenerateException {
		super.parseInternal(method, paramIndexes, batchParamIndexes);

		configUnique(method);

		configRowMapper(method);

		Query query = method.getAnnotation(Query.class);
		fetchSize = query.fetchSize();

	}

	@Override
	protected void checkBeforeParse(Method method) throws DaoGenerateException {
		super.checkBeforeParse(method);

		MapperBy mapperBy = method.getAnnotation(MapperBy.class);
		if (mapperBy == null
				&& (ClassHelper.isTypeList(method.getReturnType()) && ClassHelper.getReturnGenericType(method) == null)) {
			throw new DaoGenerateException("方法[" + method + "]配置错误：：返回值必须存在指定泛型类，或者请使用@MapperBy注解");
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void checkAfterParse(Method method) throws DaoGenerateException {
		super.checkAfterParse(method);

		MapperBy mapperBy = method.getAnnotation(MapperBy.class);
		Class<? extends RowMapper> mapperType = AutoDetectRowMapper.class;
		if (mapperBy != null) {
			mapperType = mapperBy.value();
		}

		Class<?> returnType = method.getReturnType();
		if (isUnique) {
			Class<?> expectedType = ClassHelper.getInterfaceGenericType(mapperType);
			if (!ClassHelper.isAssignable(returnType, expectedType, true)) {
				throw new DaoGenerateException("方法[" + method + "]配置错误：方法返回类型[" + returnType.getName()
						+ "]和@MappedBy注解中配置的类型[" + expectedType.getName() + "]不一致；或者请去掉@MappedBy注解");
			}
		} else {
			if (!ClassHelper.isTypeList(returnType)) {
				throw new DaoGenerateException("方法[" + method + "]配置错误：方法返回[java.util.List]类型 ，而实际返回类型["
						+ returnType.getName() + "]；或者请增加@Unique注解");
			}
		}
	}

	/**
	 * 获取查询的RowMapper配置，并初始化RowMapper实例
	 * 
	 * @param method
	 */
	@SuppressWarnings("rawtypes")
	private void configRowMapper(Method method) {
		MapperBy mapperBy = method.getAnnotation(MapperBy.class);
		Class<? extends RowMapper> mapperType = AutoDetectRowMapper.class;
		if (mapperBy != null) {
			mapperType = mapperBy.value();
		}

		if (AutoDetectRowMapper.class.equals(mapperType)) {
			Class<?> type = method.getReturnType();
			if (ClassHelper.isTypeList(type)) {
				type = ClassHelper.getReturnGenericType(method);
			}

			rowMapper = (RowMapper<?>) ReflectUtils.newInstance(mapperType, new Class[] { Class.class },
					new Object[] { type });
		} else {

			rowMapper = (RowMapper<?>) ReflectUtils.newInstance(mapperType);
		}
	}

	/**
	 * 获取查询的Unique配置，判断方法上是否有@Unique，或者返回类型是否为List
	 * 
	 * @param method
	 */
	private void configUnique(Method method) {
		Unique unique = method.getAnnotation(Unique.class);
		Class<?> returnType = method.getReturnType();
		if (unique != null || !ClassHelper.isTypeList(returnType)) {
			isUnique = true;
		} else {
			isUnique = false;
		}
	}

	public int getFetchSize() {
		return fetchSize;
	}

	public boolean isUnique() {
		return isUnique;
	}

	public RowMapper<?> getRowMapper() {
		return rowMapper;
	}

	@Override
	protected void logBindInfo(Method method) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("绑定" + getDescription() + "到方法[" + method + "]成功");
		}
	}

	private String getDescription() {
		String desc = "@Query(value=[" + this.getParsedSql().getOriginalExpression() + "],fetchSize=[" + fetchSize
				+ "]),@MapperBy(" + rowMapper.getClass() + ")";

		if (this.isUnique) {
			desc = desc + ",@Unique()";
		}

		return desc;
	}
}
