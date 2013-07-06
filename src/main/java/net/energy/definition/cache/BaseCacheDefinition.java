package net.energy.definition.cache;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import net.energy.definition.AbstractDefinition;
import net.energy.exception.DaoGenerateException;
import net.energy.expression.ExpressionParser;
import net.energy.expression.ParsedExpression;
import net.energy.expression.ParserFactory;
import net.energy.expression.ParserFactory.ExpressionType;
import net.energy.utils.Assert;
import net.energy.utils.Page;
import net.energy.utils.ReflectionUtils;

import org.apache.commons.lang.StringUtils;

/**
 * 通过对配置了@Cache，@CacheDelete，@CacheUpdate，@VerUpdate的方法的解析，
 * 产生需要在执行cache操作时必要用到的参数。 BaseCacheDefinition中配置了一些共有的或者较为基本的参数。
 * 
 * @author wuqh
 * 
 */
public abstract class BaseCacheDefinition extends AbstractDefinition {
	BaseCacheDefinition() throws DaoGenerateException {
		// 使用空方法作为参数，延迟初始化
		super(null);
	}

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
	 * 缓存类的绑定关系在CacheDefinitionCollection中进行记录，所以子类不需要实现，进行覆盖
	 * 
	 */
	@Override
	protected void logBindInfo(Method method) {
	}

	@Override
	protected ParsedExpression parseExpression(Method method) {
		ExpressionParser parser = ParserFactory.createExpressionParser(ExpressionType.CACHE_KEY);

		ParsedExpression parsedKey = parser.parse(getSourceKey());

		return parsedKey;
	}

	/**
	 * 获取方法中配置的原始CacheKey
	 * 
	 * @return
	 */
	protected abstract String getSourceKey();

	/**
	 * 生成缓存的基本key值，这个过程只是将@Param中的值，结合key、vkey的配置来产生缓存key值，并不包含分页等的判断。
	 * 
	 * @param args
	 * @param parsedExpression
	 * @param getterMethods
	 * @param parameterIndexes
	 * @return
	 */
	private String buildSimpleKey(Object[] args, ParsedExpression parsedExpression, Method[] getterMethods,
			Integer[] parameterIndexes) {
		Assert.notNull(parsedExpression, "ParsedExpression的值不能为null");

		String oriKey = parsedExpression.getOriginalExpression();
		List<String> parameterNames = parsedExpression.getParameterNames();

		int length = parameterNames.size();

		int lastIndex = 0;
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < length; i++) {
			// 根据每个参数的起止位置，进行substring操作，将参数部分用实际值来不断替换，拼接key值。
			int[] indexes = parsedExpression.getParameterIndexes(i);

			int startIndex = indexes[0];
			int endIndex = indexes[1];
			builder.append(oriKey.substring(lastIndex, startIndex));

			Method method = getterMethods[i];
			Integer index = parameterIndexes[i];

			Object value = ReflectionUtils.fetchValue(method, index, args, parameterNames);
			if (value != null) {
				builder.append(value.toString());
			}

			lastIndex = endIndex;
		}

		builder.append(oriKey.substring(lastIndex));

		return builder.toString();
	}

	/**
	 * 生成最终用于查询的key值，生成时会判断是否为分页查询，并自动加上-pN用于区分
	 * 
	 * @param args
	 * @return
	 */
	public String generateCacheKey(Object[] args) {
		String oriKey = buildSimpleKey(args, getParsedKey(), getKeyGetterMethods(), getKeyParameterIndexes());

		StringBuilder cacheKey = new StringBuilder();

		cacheKey.append(oriKey);

		Page page = getPageArgument(args);
		if (page != null) {
			cacheKey.append("-p").append(page.getCurpage());
		}

		return cacheKey.toString();
	}

	/**
	 * 生成缓存版本号的key值，这和生成缓存的key值几乎是一样的
	 * 
	 * @param args
	 * @return
	 */
	public String generateVersionKey(Object[] args) {
		String oriKey = buildSimpleKey(args, getParsedVkey(), getVkeyGetterMethods(), getVkeyParameterIndexes());

		return oriKey;
	}

	/**
	 * 生成分页对象缓存的key值，即page-拼接上对象的缓存key值
	 * 
	 * @param args
	 * @return
	 */
	public String generatePageKey(Object[] args, String key) {
		Page page = getPageArgument(args);
		if (page == null) {
			return null;
		}
		if (StringUtils.isEmpty(key)) {
			key = generateCacheKey(args);
		}
		if (StringUtils.isEmpty(key)) { // 无法创建key
			return null;
		}
		StringBuilder builder = new StringBuilder("page-");
		builder.append(key);
		return builder.toString();
	}

	protected abstract ParsedExpression getParsedKey();

	protected abstract Method[] getKeyGetterMethods();

	protected abstract Integer[] getKeyParameterIndexes();

	protected abstract ParsedExpression getParsedVkey();

	protected abstract Method[] getVkeyGetterMethods();

	protected abstract Integer[] getVkeyParameterIndexes();
}
