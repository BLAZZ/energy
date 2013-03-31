package net.energy.jdbc.definition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import net.energy.annotation.BatchParam;
import net.energy.annotation.GenericTable;
import net.energy.annotation.Param;
import net.energy.exception.DaoGenerateException;
import net.energy.expression.ExpressionParser;
import net.energy.expression.ParsedExpression;
import net.energy.expression.ParserFacotory;
import net.energy.expression.ParserFacotory.ExpressionType;
import net.energy.utils.CommonUtils;
import net.energy.utils.Page;
import net.energy.utils.SqlFormatter;

import org.apache.commons.lang.ArrayUtils;

/**
 * 通过对配置了@Query，@Update，@BatchUpdate的方法的解析， 产生需要在执行JDBC操作时必要用到的参数。
 * BaseJdbcDefinition中配置了一些共有的或者较为基本的参数。
 * 
 * @author wuqh
 * 
 */
public class BaseJdbcDefinition {
	/**
	 * 解析后的SQL值对象
	 */
	private ParsedExpression parsedSql;
	/**
	 * 需要创建PreparedStatement的SQL
	 */
	private String preparedSql;
	/**
	 * 用于从方法参数的Bean对象中获取需要变量值的getter方法。缓存起来用于减少反射的查询操作
	 */
	private Method[] getterMethods;
	/**
	 * parsedSql的parameterNames中每个name对应对象在方法args[]数组中的索引值。
	 * 由于parameterNames包含"."，所以需要parameterIndexes记录"."之前的@Param、@BatchParam对应的位置
	 * 
	 * <pre>
	 * 例如：
	 * <code>@BatchUpdate("insert into photo(ownerId, albumId, file) values (:user.id, :album.id, :photo.file)")</code>
	 * <code>@ReturnId
	 * public List<Long> insertPhotosReturnIds(@Param("user") User user, @Param("album") Album album, @BatchParam("photo") Photo[] photo);</code>
	 * 将解析出parsedSql.parameterNames=["user.id","album.id","photo.file"]，但对应的parameterIndexes就会是[0,1,2]
	 * 
	 * 注意：@BatchParam对应的index也会当做@Param处理。
	 * 因为，args[]=[1,2,[3,4]]这样的参数在调用时会转换为[[1,2,3],[1,2,4]]给BatchSQL调用。
	 * 所以，对于@BatchParam中的每一个值，在实际调用过程中都相当于每次都是一个@Param
	 * </pre>
	 * 
	 */
	private Integer[] parameterIndexes;
	/**
	 * 通用表名在参数的位置信息。
	 * 
	 * <pre>
	 * 例如：
	 * <code>@Query("select * from {0} where id=:id")</code>
	 * <code>public List queryCommentByResource(@Param("id") Object resourceId, @GenericTable(index=0) String commentType);</code>
	 * 将解析出的genericIndexes=[1]。
	 * 同理，如果存在一个{N}，那么genericIndexes[N]=（存在@GenericTable(index=N)annotation的参数在args中的index值）
	 * </pre>
	 * 
	 */
	private Integer[] genericIndexes;
	/**
	 * 由于批量执行过程中，需要逐一替换参数中对映位置的值，所以需要记录每一个@BatchParam在args中的index
	 * 
	 * <pre>
	 * 例如：
	 * <code>@BatchUpdate("insert into photo(ownerId, albumId, file) values (:user.id, :album.id, :photo.file)")</code>
	 * <code>@ReturnId
	 * public List<Long> insertPhotosReturnIds(@Param("user") User user, @Param("album") Album album, @BatchParam("photo") Photo[] photo);</code>
	 * 对应的batchParamIndexes就会是[3]
	 * </pre>
	 * 
	 */
	private Integer[] batchParamIndexes;

	public String getPreparedSql() {
		return preparedSql;
	}

	public Method[] getGetterMethods() {
		return getterMethods;
	}

	public Integer[] getParameterIndexes() {
		return parameterIndexes;
	}

	public Integer[] getGenericIndexes() {
		return genericIndexes;
	}

	public Integer[] getBatchParamIndexes() {
		return batchParamIndexes;
	}

	public ParsedExpression getParsedSql() {
		return parsedSql;
	}

	public void setParsedSql(ParsedExpression parsedSql) {
		this.parsedSql = parsedSql;
	}

	/**
	 * 解析方法参数上配置的所有annotation，以及一些特殊的参数类型，例如{@link Page}
	 * parameter上的annotation为一个二维数组，一个维度为参数个数，第二个对单个参数上的所有annotation
	 * 即：annotations[0]，即为args[0]上的所有参数。
	 * 
	 * @param annotations
	 *            parameter上的annotation
	 * @param paramIndexes
	 *            每个@Param参数在parameter数组中的位置，如@Param("user")在parameter[0]上，那么
	 *            paramIndexes.get("user")=0
	 * @param batchParamIndexMap
	 *            每个@BatchParam参数在parameter数组中的位置，如@BatchParam("user")在parameter
	 *            [0]上，那么 batchParamIndexMap("user")=0
	 * @param paramTypes
	 *            parameter的每个参数的类型
	 * 
	 */
	protected void parseParameterAnnotations(Annotation[][] annotations, Map<String, Integer> paramIndexes,
			Map<String, Integer> batchParamIndexMap, Class<?>[] paramTypes) throws DaoGenerateException {
		for (int index = 0; index < annotations.length; index++) {

			for (Annotation annotation : annotations[index]) {
				Class<? extends Annotation> annotationType = annotation.annotationType();
				if (Param.class.equals(annotationType)) {
					Param param = (Param) annotation;
					String value = param.value();
					paramIndexes.put(value, index);
				}
				if (BatchParam.class.equals(annotationType) && batchParamIndexMap != null) {
					BatchParam param = (BatchParam) annotation;
					String value = param.value();
					batchParamIndexMap.put(value, index);
					batchParamIndexes = (Integer[]) ArrayUtils.add(batchParamIndexes, new Integer(index));

					if (paramTypes[index] == null || !paramTypes[index].isArray()) {
						throw new DaoGenerateException("@BatchParam can only on an array");
					}

				}
				if (GenericTable.class.equals(annotationType)) {
					GenericTable genericTable = (GenericTable) annotation;
					int order = genericTable.index();
					genericIndexes = CommonUtils.addElemToArray(genericIndexes, index, order);
				}
			}
		}
	}

	/**
	 * 方法{@link BaseJdbcDefinition#parseSql(String, Class[], Map, Map)}
	 * 的默认调用，采用非batch的方式。
	 * 
	 * @param oriSql
	 *            <code>@Query</code>等中配置的SQL
	 * @param paramTypes
	 *            parameter的每个参数的类型
	 * @param paramIndexes
	 *            每个@Param参数在parameter数组中的位置，如@Param("user")在parameter[0]上，那么
	 *            paramIndexes.get("user")=0
	 * @throws DaoGenerateException
	 */
	protected void parseSql(String oriSql, Class<?>[] paramTypes, Map<String, Integer> paramIndexes)
			throws DaoGenerateException {
		parseSql(oriSql, paramTypes, paramIndexes, null);
	}

	/**
	 * 根据原始的SQL等生成解析后的parsedSql、获取参数必须的getter方法、parameterIndexes、以及preparedSql（
	 * 未经表名替换）
	 * 
	 * @param oriSql
	 *            <code>@Query</code>等中配置的SQL
	 * @param paramTypes
	 *            parameter的每个参数的类型
	 * @param paramIndexes
	 *            每个@Param参数在parameter数组中的位置，如@Param("user")在parameter[0]上，那么
	 *            paramIndexes.get("user")=0
	 * @param batchParamIndexes
	 *            每个@BatchParam参数在parameter数组中的位置，如@BatchParam("user")在parameter
	 *            [0]上，那么 batchParamIndexMap("user")=0
	 * @throws DaoGenerateException
	 */
	protected void parseSql(String oriSql, Class<?>[] paramTypes, Map<String, Integer> paramIndexes,
			Map<String, Integer> batchParamIndexes) throws DaoGenerateException {
		ExpressionParser parser = ParserFacotory.createExpressionParser(ExpressionType.SQL);
		parsedSql = parser.parse(oriSql);
		List<String> parameterNames = parsedSql.getParameterNames();
		Object[] gettersAndIndexes = null;
		if (batchParamIndexes == null) {
			gettersAndIndexes = CommonUtils.getGettersAndIndexes(parameterNames, paramIndexes, paramTypes);
		} else {
			gettersAndIndexes = CommonUtils.getGettersAndIndexes(parameterNames, paramIndexes, batchParamIndexes,
					paramTypes);
		}
		Method[] getterMethods = (Method[]) gettersAndIndexes[0];
		Integer[] parameterIndexes = (Integer[]) gettersAndIndexes[1];

		String preparedSql = CommonUtils.getSql(parsedSql);

		this.getterMethods = getterMethods;
		this.parameterIndexes = parameterIndexes;
		this.preparedSql = preparedSql;
	}

	/**
	 * 替换SQL中的通用表名，获取实际用于执行的PreparedSQL
	 * 
	 * @param args
	 * @return
	 */
	public String getActualSql(Object[] args) {
		String[] tableNames = null;
		String sql;

		if (genericIndexes != null) {
			for (Integer index : genericIndexes) {
				if (index != null) {
					Object arg = args[index];
					if (arg != null) {
						tableNames = (String[]) ArrayUtils.add(tableNames, arg.toString());
					}
				}
			}
		}

		if (tableNames != null) {
			sql = SqlFormatter.formatSql(preparedSql, tableNames);
		} else {
			sql = preparedSql;
		}

		return sql;
	}

}
