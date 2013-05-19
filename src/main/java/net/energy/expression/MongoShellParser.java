package net.energy.expression;

import java.util.HashSet;
import java.util.Set;

import net.energy.utils.Assert;

/**
 * Mongo的Shell解析器
 * 
 * @author wuqh
 *
 */
public class MongoShellParser extends AbstractExpressionParser {
	private final char[] parameterSeparators = new char[] { '{', '}', ':', '&', ',', ';', '"', '\'', '[', ']' };

	private final String[] startSkip = new String[] { "/*" };

	private final String[] stopSkip = new String[] { "*/" };

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

	public ParsedExpression parse(String shell) {
		return parseShell(shell);
	}

	private ParsedExpression parseShell(String shell) {
		Assert.notNull(shell, "Shell must not be null");

		Set<String> namedParameters = new HashSet<String>();
		ParsedExpression parsedShell = new ParsedExpression(shell);

		char[] shellChars = shell.toCharArray();
		int namedParameterCount = 0;
		int unnamedParameterCount = 0;
		int totalParameterCount = 0;

		int i = 0;
		while (i < shellChars.length) {
			//获取跳过注释后的有效表达式起始位置
			int skipToPosition = skipCommentsAndQuotes(shellChars, i);
			if (i != skipToPosition) {
				if (skipToPosition >= shellChars.length) {
					break;
				}
				i = skipToPosition;
			}
			char c = shellChars[i];
			// 判断当前的字符是否为参数起始符":"
			if (c == ':') {
				int j = i + 1;
				// 如果有连续多个“:”，只有最后一个才会作为参数的起始符
				if (j < shellChars.length && shellChars[j] == ':' && c == ':') {
					i++;
					continue;
				}
				j = endIndexOfParameter(shellChars, i, j);
				if (j - i > 1) {
					String parameter = shell.substring(i + 1, j);
					namedParameterCount = addNamedParameter(namedParameters, parameter, namedParameterCount);
					parsedShell.addNamedParameter(parameter, i, j);
					totalParameterCount++;
				}
				i = j - 1;
			}
			i++;
		}
		parsedShell.setNamedParameterCount(namedParameterCount);
		parsedShell.setUnnamedParameterCount(unnamedParameterCount);
		parsedShell.setTotalParameterCount(totalParameterCount);
		return parsedShell;
	}
}
