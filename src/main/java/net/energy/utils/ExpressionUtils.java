package net.energy.utils;

import java.util.List;

import net.energy.expression.ParsedExpression;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 表达式工具类
 * 
 * @author wuqh
 *
 */
public final class ExpressionUtils {
	static final Log LOGGER = LogFactory.getLog(ExpressionUtils.class);
	public static final String SHELL_TOKEN = "#";
	private static final String SQL_TOKEN = "?";
	
	/**
	 * 生成PreparedSQL
	 * 
	 * @param parsedExpression
	 * @return
	 */
	public static String getSql(ParsedExpression parsedExpression) {
		return getExpressionWithToken(parsedExpression, SQL_TOKEN);
	}

	/**
	 * 生成MongoShell
	 * 
	 * @param parsedExpression
	 * @return
	 */
	public static String getShell(ParsedExpression parsedExpression) {
		return getExpressionWithToken(parsedExpression, SHELL_TOKEN);
	}

	/**
	 * 生成表达式，将parsedExpression中的参数使用token来替换
	 * 
	 * @param parsedExpression
	 * @param token
	 * @return
	 */
	private static String getExpressionWithToken(ParsedExpression parsedExpression, String token) {
		String originalExpression = parsedExpression.getOriginalExpression();
		StringBuilder actualExpression = new StringBuilder();
		List<String> paramNames = parsedExpression.getParameterNames();
		int lastIndex = 0;
		for (int i = 0; i < paramNames.size(); i++) {
			int[] indexes = parsedExpression.getParameterIndexes(i);
			int startIndex = indexes[0];
			int endIndex = indexes[1];
			actualExpression.append(originalExpression.substring(lastIndex, startIndex));
			actualExpression.append(token);
			lastIndex = endIndex;
		}
		actualExpression.append(originalExpression.substring(lastIndex, originalExpression.length()));
		return actualExpression.toString();
	}
}
