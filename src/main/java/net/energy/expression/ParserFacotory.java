package net.energy.expression;

/**
 * 解析器生成器
 * 
 * @author wuqh
 * 
 */
public final class ParserFacotory {
	private static final SqlParser SQL_PARSER = new SqlParser();
	private static final CacheKeyParser KEY_PARSER = new CacheKeyParser();
	private static final MongoShellParser SHELL_PARSER = new MongoShellParser();

	private ParserFacotory() {
	}

	/**
	 * 根据表单式的类型获取表达式的解析器
	 * 
	 * @param type
	 * @return
	 */
	public static ExpressionParser createExpressionParser(ExpressionType type) {
		switch (type) {
		case SQL:
			return SQL_PARSER;
		case CACHE_KEY:
			return KEY_PARSER;
		case MONGO_SHELL:
			return SHELL_PARSER;
		default:
			return null;
		}
	}

	/**
	 * 表单式类型：SQL，CACHE的KEY，MONGO的SHELL
	 * 
	 * @author wuqh
	 * 
	 */
	public enum ExpressionType {
		SQL, CACHE_KEY, MONGO_SHELL
	}
}
