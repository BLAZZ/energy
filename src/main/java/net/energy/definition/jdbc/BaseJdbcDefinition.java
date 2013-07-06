package net.energy.definition.jdbc;

import java.lang.reflect.Method;
import java.util.Map;

import net.energy.definition.AbstractDefinition;
import net.energy.exception.DaoGenerateException;
import net.energy.expression.ExpressionParser;
import net.energy.expression.ParsedExpression;
import net.energy.expression.ParserFactory;
import net.energy.expression.ParserFactory.ExpressionType;
import net.energy.utils.ArrayHelper;
import net.energy.utils.ExpressionUtils;
import net.energy.utils.SqlFormatter;

/**
 * 通过对配置了@Query，@Update，@BatchUpdate的方法的解析， 产生需要在执行JDBC操作时必要用到的参数。
 * BaseJdbcDefinition中配置了一些共有的或者较为基本的参数。
 * 
 * @author wuqh
 * 
 */
public abstract class BaseJdbcDefinition extends AbstractDefinition {

	BaseJdbcDefinition(Method method) throws DaoGenerateException {
		super(method);
	}

	/**
	 * 需要创建PreparedStatement的SQL
	 */
	private String preparedSql;

	/**
	 * 获取解析后的SQL表达式
	 * 
	 * @return
	 */
	public ParsedExpression getParsedSql() {
		return getParsedExpression();
	}

	@Override
	protected ParsedExpression parseExpression(Method method) {
		ExpressionParser parser = ParserFactory.createExpressionParser(ExpressionType.SQL);

		ParsedExpression parsedSql = parser.parse(getSourceSql(method));
		preparedSql = ExpressionUtils.getSql(parsedSql);

		return parsedSql;
	}

	/**
	 * 获取方法中配置的原始SQL
	 * 
	 * @param method
	 * @return
	 */
	protected abstract String getSourceSql(Method method);

	@Override
	protected void checkBeforeParse(Method method) throws DaoGenerateException {
	}

	@Override
	protected void parseInternal(Method method, Map<String, Integer> paramIndexes,
			Map<String, Integer> batchParamIndexes) throws DaoGenerateException {
	}

	@Override
	protected void checkAfterParse(Method method) throws DaoGenerateException {
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
						tableNames = (String[]) ArrayHelper.add(tableNames, arg.toString());
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
