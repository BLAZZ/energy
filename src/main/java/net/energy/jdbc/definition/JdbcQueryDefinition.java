package net.energy.jdbc.definition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.energy.annotation.Unique;
import net.energy.annotation.jdbc.MapperBy;
import net.energy.annotation.jdbc.Query;
import net.energy.exception.DaoGenerateException;
import net.energy.jdbc.RowMapper;
import net.energy.utils.GenericUtils;
import net.energy.utils.TypeUtils;
import net.sf.cglib.core.ReflectUtils;

import org.apache.commons.lang.ClassUtils;

/**
 * 通过对配置了@Query的方法的解析，产生需要在执行JDBC操作时必要用到的参数。
 * 
 * @author wuqh
 * 
 */
public class JdbcQueryDefinition extends BaseJdbcDefinition {
	private int fetchSize;
	private int pageIndex = -1;
	private boolean isUnique = false;
	private RowMapper<?> rowMapper;

	public JdbcQueryDefinition(Method method) throws DaoGenerateException {
		init(method);
	}

	private void init(Method method) throws DaoGenerateException {
		configUnique(method);

		configRowMapper(method);

		Query query = method.getAnnotation(Query.class);
		fetchSize = query.fetchSize();

		Map<String, Integer> paramIndexes = new HashMap<String, Integer>(8, 1f);

		Class<?>[] paramTypes = method.getParameterTypes();
		Annotation[][] annotations = method.getParameterAnnotations();
		for (int index = 0; index < annotations.length; index++) {
			if (TypeUtils.isTypePage(paramTypes[index])) {
				pageIndex = index;
				break;
			}
		}

		parseParameterAnnotations(annotations, paramIndexes, null, paramTypes);

		String sql = query.value();
		parseSql(sql, paramTypes, paramIndexes);

		// parseShardBy(method, paramIndexes, paramTypes);
	}

	/**
	 * 获取查询的RowMapper配置，由于需要根据@Unique判断返回值类型，所以需要在
	 * {@link JdbcQueryDefinition#configUnique(Method)}后执行。 此方法进行以下几项操作：
	 * 1、根据@MapperBy初始化 RowMapper实例。如果不存在@MapperBy会抛出DaoGenerateException；
	 * 2、检查返回类型：如果配置@Unique了，返回类型必须@MapperBy的泛型类型相同；如果没有配置@Unique就必须返回List
	 * 
	 * @param method
	 * @throws DaoGenerateException
	 */
	private void configRowMapper(Method method) throws DaoGenerateException {
		MapperBy mapperBy = method.getAnnotation(MapperBy.class);
		Class<? extends RowMapper<?>> mapperType = null;
		if (mapperBy == null) {
			throw new DaoGenerateException("Missing @MapperBy on @Query");
		} else {
			mapperType = mapperBy.value();
		}
		rowMapper = (RowMapper<?>) ReflectUtils.newInstance(mapperType);

		Class<?> returnType = method.getReturnType();
		if (isUnique) {
			Class<?> expectedType = GenericUtils.getGenericType(mapperType);
			if (!ClassUtils.isAssignable(returnType, expectedType, true)) {
				throw new DaoGenerateException("Return type is expected as '" + expectedType.getName()
						+ "' with @Unique, but actually is '" + returnType.getName() + "' in method: "
						+ method.toString() + ", missing @MappedBy");
			}
		} else {
			if (!TypeUtils.isTypeList(returnType)) {
				throw new DaoGenerateException(
						"Return type is expected as 'java.util.List' without @Unique, but actually is '"
								+ returnType.getName() + "' in method: " + method.toString());
			}
		}
	}

	/**
	 * 获取查询的Unique配置，判断方法上是否有@Unique
	 * 
	 * @param method
	 */
	private void configUnique(Method method) {
		Unique unique = method.getAnnotation(Unique.class);
		if (unique != null) {
			isUnique = true;
		} else {
			isUnique = false;
		}
	}

	public int getFetchSize() {
		return fetchSize;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public boolean isUnique() {
		return isUnique;
	}

	public RowMapper<?> getRowMapper() {
		return rowMapper;
	}

}
