package net.energy.expression;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析后的表达式，包含一些解析后的参数名，参数的起止位置等信息
 * 
 * @author wuqh
 * 
 */
public class ParsedExpression {
	/**
	 * 原始表达式
	 */
	private final String originalExpression;
	/**
	 * 参数的名称
	 */
	private final List<String> parameterNames = new ArrayList<String>();
	/**
	 * 参数的起止位置
	 */
	private final List<int[]> parameterIndexes = new ArrayList<int[]>();

	public ParsedExpression(String originalExpression) {
		this.originalExpression = originalExpression;
	}

	public String getOriginalExpression() {
		return originalExpression;
	}

	void addNamedParameter(String parameterName, int startIndex, int endIndex) {
		this.parameterNames.add(parameterName);
		this.parameterIndexes.add(new int[] { startIndex, endIndex });
	}

	public List<String> getParameterNames() {
		return this.parameterNames;
	}

	public int[] getParameterIndexes(int parameterPosition) {
		return this.parameterIndexes.get(parameterPosition);
	}

}
