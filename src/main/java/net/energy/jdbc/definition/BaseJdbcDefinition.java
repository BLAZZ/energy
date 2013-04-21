package net.energy.jdbc.definition;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import net.energy.exception.DaoGenerateException;
import net.energy.expression.ExpressionParser;
import net.energy.expression.ParsedExpression;
import net.energy.expression.ParserFacotory;
import net.energy.expression.ParserFacotory.ExpressionType;
import net.energy.utils.CommonUtils;
import net.energy.utils.ParameterParseable;
import net.energy.utils.SqlFormatter;

import org.apache.commons.lang.ArrayUtils;

/**
 * 通过对配置了@Query，@Update，@BatchUpdate的方法的解析， 产生需要在执行JDBC操作时必要用到的参数。
 * BaseJdbcDefinition中配置了一些共有的或者较为基本的参数。
 * 
 * @author wuqh
 * 
 */
public class BaseJdbcDefinition extends ParameterParseable {
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

	public String getPreparedSql() {
		return preparedSql;
	}

	public Method[] getGetterMethods() {
		return getterMethods;
	}

	public ParsedExpression getParsedSql() {
		return parsedSql;
	}

	public void setParsedSql(ParsedExpression parsedSql) {
		this.parsedSql = parsedSql;
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
