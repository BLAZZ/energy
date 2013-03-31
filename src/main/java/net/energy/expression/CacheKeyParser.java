package net.energy.expression;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.energy.utils.Assert;

/**
 * Cache的Key解析器
 * 
 * @author wuqh
 * 
 */
public class CacheKeyParser extends AbstractExpressionParser {
	private static final int DEFAULT_CACHE_LIMIT = 256;
	private final char[] parameterSeparators = new char[] { '-', ':', '&', ',', ';', '"', '\'', '=', '+', };

	private final String[] startSkip = new String[] { "'", "\"", "/*" };

	private final String[] stopSkip = new String[] { "'", "\"", "*/" };

	private static final Map<String, ParsedExpression> PARSEDE_KEY_CACHE = new HashMap<String, ParsedExpression>(
			DEFAULT_CACHE_LIMIT);

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

	public ParsedExpression parse(String key) {
		ParsedExpression parsedKey = PARSEDE_KEY_CACHE.get(key);
		if (parsedKey == null) {
			parsedKey = parseCacheKey(key);
			PARSEDE_KEY_CACHE.put(key, parsedKey);
		}
		return parsedKey;
	}

	private ParsedExpression parseCacheKey(String key) {
		Assert.notNull(key, "Cache Key must not be null");

		Set<String> namedParameters = new HashSet<String>();
		ParsedExpression parsedKey = new ParsedExpression(key);

		char[] keyChars = key.toCharArray();
		int namedParameterCount = 0;
		int unnamedParameterCount = 0;
		int totalParameterCount = 0;

		int i = 0;
		while (i < keyChars.length) {
			// 获取跳过注释后的有效表达式起始位置
			int skipToPosition = skipCommentsAndQuotes(keyChars, i);
			if (i != skipToPosition) {
				if (skipToPosition >= keyChars.length) {
					break;
				}
				i = skipToPosition;
			}
			char c = keyChars[i];
			// 判断当前的字符是否为参数起始符":"
			if (c == ':') {
				int j = i + 1;
				// 如果有连续多个“:”，只有最后一个才会作为参数的起始符
				if (j < keyChars.length && keyChars[j] == ':' && c == ':') {
					i++;
					continue;
				}
				j = endIndexOfParameter(keyChars, i, j);
				if (j - i > 1) {
					String parameter = key.substring(i + 1, j);
					namedParameterCount = addNamedParameter(namedParameters, parameter, namedParameterCount);
					parsedKey.addNamedParameter(parameter, i, j);
					totalParameterCount++;
				}
				i = j - 1;
			}
			i++;
		}
		parsedKey.setNamedParameterCount(namedParameterCount);
		parsedKey.setUnnamedParameterCount(unnamedParameterCount);
		parsedKey.setTotalParameterCount(totalParameterCount);
		return parsedKey;
	}
}
