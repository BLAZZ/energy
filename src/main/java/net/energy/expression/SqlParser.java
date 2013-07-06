package net.energy.expression;

import java.util.HashSet;
import java.util.Set;

import net.energy.utils.Assert;

/**
 * SQL解析器
 * 
 * @author wuqh
 * 
 */
public class SqlParser extends AbstractExpressionParser {
	private final char[] parameterSeparators = new char[] { '"', '\'', ':', '&', ',', ';', '(', ')', '|', '=', '+',
			'-', '*', '%', '/', '\\', '<', '>', '^' };

	private final String[] startSkip = new String[] { "'", "\"", "--", "/*" };

	private final String[] stopSkip = new String[] { "'", "\"", "\n", "*/" };

	@Override
	protected char[] getParameterSeparators() {
		return parameterSeparators;
	}

	@Override
	protected String[] getStartSkip() {
		return startSkip;
	}

	@Override
	protected String[] getStopSkip() {
		return stopSkip;
	}

	public ParsedExpression parse(String sql) {
		return parseSql(sql);
	}

	private ParsedExpression parseSql(String sql) {
		Assert.notNull(sql, "SQL must not be null");

		Set<String> namedParameters = new HashSet<String>();
		ParsedExpression parsedSql = new ParsedExpression(sql);

		char[] statement = sql.toCharArray();
		int namedParameterCount = 0;
		// int unnamedParameterCount = 0;
		// int totalParameterCount = 0;

		int i = 0;
		while (i < statement.length) {
			// 获取跳过注释后的有效表达式起始位置
			int skipToPosition = skipCommentsAndQuotes(statement, i);
			if (i != skipToPosition) {
				if (skipToPosition >= statement.length) {
					break;
				}
				i = skipToPosition;
			}
			char c = statement[i];
			// 判断当前的字符是否为参数起始符":"或者"&"
			if (c == ':' || c == '&') {
				int j = i + 1;
				if (j < statement.length && statement[j] == ':' && c == ':') {
					// Postgres-style "::" casting operator - to be skipped.
					i = i + 2;
					continue;
				}
				j = endIndexOfParameter(statement, j);
				if (j - i > 1) {
					String parameter = sql.substring(i + 1, j);
					namedParameterCount = addNamedParameter(namedParameters, parameter, namedParameterCount);
					parsedSql.addNamedParameter(parameter, i, j);
					// totalParameterCount++;
				}
				i = j - 1;
			}
			// else if (c == '?') {
			// unnamedParameterCount++;
			// totalParameterCount++;
			// }
			i++;
		}
		// parsedSql.setNamedParameterCount(namedParameterCount);
		// parsedSql.setUnnamedParameterCount(unnamedParameterCount);
		// parsedSql.setTotalParameterCount(totalParameterCount);
		return parsedSql;
	}

}
