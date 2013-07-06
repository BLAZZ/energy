package net.energy.expression;

import java.util.Set;

/**
 * ExpressionParser的抽象实现类，包含了一些表达式解析过程中需要用到的共用方法。
 * 
 * @author wuqh
 * 
 */
abstract class AbstractExpressionParser implements ExpressionParser {
	/**
	 * 获取参数分隔符。即：参数起始符“:”到这个数组中的值之间的部分就是参数
	 * 
	 * @return
	 */
	protected abstract char[] getParameterSeparators();

	/**
	 * 注释结束标志，即注释结束，之后的内容需要继续进行解析。getStopSkip()[N]必须和getStartSkip()[N]配对
	 * 
	 * @return
	 */
	protected abstract String[] getStopSkip();

	/**
	 * 注释起始标志，遇到数组中的值表示之后的一段是注释，可以跳过解析。getStopSkip()[N]必须和getStartSkip()[N]配对
	 * 
	 * @return
	 */
	protected abstract String[] getStartSkip();

	/**
	 * 将parameter放到namedParameters中，并将namedParameterCount+1
	 * 
	 * @param namedParameters
	 * @param parameter
	 * @param namedParameterCount
	 * @return
	 */
	int addNamedParameter(Set<String> namedParameters, String parameter, int namedParameterCount) {
		if (!namedParameters.contains(parameter)) {
			namedParameters.add(parameter);
			namedParameterCount++;
		}
		return namedParameterCount;
	}

	/**
	 * 获取参数的结束位置
	 * 
	 *
	 * @param expressionChars
	 *            表达式内容
	 * @param j
	 *            参数起始符位置在expressionChars中的index
	 * @return 参数的结束位置
	 */
	int endIndexOfParameter(char[] expressionChars, int j) {
		while (j < expressionChars.length && !isParameterSeparator(expressionChars[j])) {
			j++;
		}
		return j;
	}

	/**
	 * 跳过表达式中的注释部分
	 * 
	 * @param expressionChars
	 *            表达式内容
	 * @param position
	 *            当前内容在expressionChars中的index
	 * @return 跳过注释后的有效表达式起始位置
	 */
	int skipCommentsAndQuotes(char[] expressionChars, int position) {
		String[] startSkip = getStartSkip();

		for (int i = 0; i < startSkip.length; i++) {
			// 判断表达是否和注释的起始标识匹配
			if (expressionChars[position] == startSkip[i].charAt(0)) {
				boolean match = true;
				for (int j = 1; j < startSkip[i].length(); j++) {
					if (!(expressionChars[position + j] == startSkip[i].charAt(j))) {
						match = false;
						break;
					}
				}
				// 如果匹配需要返回跳过后的表达式位置
				if (match) {
					return endIndexOfCommentsAndQuotes(expressionChars, i, position);
				}

			}
		}
		return position;
	}

	/**
	 * 根据注释起始的index，获取对应的跳过，注释后的有效表达式起始位置。
	 * 
	 * @param expressionChars
	 *            表达式内容
	 * @param i
	 *            注释起始标识在getStartSkip()中的index
	 * @param position
	 *            当前内容在expressionChars中的index
	 * @return 跳过注释后的有效表达式起始位置
	 */
	int endIndexOfCommentsAndQuotes(char[] expressionChars, int i, int position) {
		String[] startSkip = getStartSkip();
		String[] stopSkip = getStopSkip();

		int offset = startSkip[i].length();
		for (int m = position + offset; m < expressionChars.length; m++) {
			// 由于注释起始标识在getStartSkip()中index为i,所以结束标识必然是getStopSkip()中index为i的那个。
			if (expressionChars[m] == stopSkip[i].charAt(0)) {
				boolean endMatch = true;
				int endPos = m;
				for (int n = 1; n < stopSkip[i].length(); n++) {
					if (m + n >= expressionChars.length) {
						// last comment not closed properly
						return expressionChars.length;
					}
					if (!(expressionChars[m + n] == stopSkip[i].charAt(n))) {
						endMatch = false;
						break;
					}
					endPos = m + n;
				}
				if (endMatch) {
					// found character sequence ending comment or quote
					return endPos + 1;
				}
			}
		}
		// character sequence ending comment or quote not found
		return expressionChars.length;
	}

	/**
	 * 判断字符是否为解析参数的分隔符：空格或者getParameterSeparators()中的值
	 * 
	 * @param c
	 * @return
	 */
	boolean isParameterSeparator(char c) {
		if (Character.isWhitespace(c)) {
			return true;
		}

		char[] parameterSeparators = getParameterSeparators();

		for (char separator : parameterSeparators) {
			if (c == separator) {
				return true;
			}
		}
		return false;
	}
}
