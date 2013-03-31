package net.energy.expression;

/**
 * 表单式解析接口，表达式可以是cache的key，sql，mongo的shell等
 * 
 * @author wuqh
 * 
 */
public interface ExpressionParser {
	/**
	 * 解析表达式，获取表单式中的参数变量信息
	 * 
	 * @param expression
	 * @return
	 */
	public ParsedExpression parse(String expression);
}
