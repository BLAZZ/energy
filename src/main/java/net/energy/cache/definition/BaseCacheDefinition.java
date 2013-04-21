package net.energy.cache.definition;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.energy.exception.DaoGenerateException;
import net.energy.expression.ExpressionParser;
import net.energy.expression.ParsedExpression;
import net.energy.expression.ParserFacotory;
import net.energy.expression.ParserFacotory.ExpressionType;
import net.energy.utils.Assert;
import net.energy.utils.CommonUtils;
import net.energy.utils.Page;
import net.energy.utils.ParameterParseable;
import net.energy.utils.TypeUtils;

import org.apache.commons.lang.StringUtils;

/**
 * 通过对配置了@Cache，@CacheDelete，@CacheUpdate，@VerUpdate的方法的解析，
 * 产生需要在执行cache操作时必要用到的参数。 BaseCacheDefinition中配置了一些共有的或者较为基本的参数。
 * 
 * @author wuqh
 * 
 */
public class BaseCacheDefinition extends ParameterParseable {
	/**
	 * 解析后的key值对象
	 */
	private ParsedExpression parsedKey;
	/**
	 * 用于从方法参数的Bean对象中获取需要的key的变量值的getter方法。缓存起来用于减少反射的查询操作
	 */
	private Method[] keyGetterMethods;
	/**
	 * parsedKey的parameterNames中每个name对应对象在方法args[]数组中的索引值。
	 * 由于parameterNames包含"."，所以需要keyParameterIndexes记录"."之前的@Param对应的位置
	 * 
	 * <pre>
	 * 例如：
	 * <code>@CacheDelete(key="album-d-:albumId-:ownerId")</code>
	 * <code>@VerUpdate(vkey="ALBUM-:owner.id-:album.id-v")</code>
	 * public void updatePhoto(@Param("owner") User owner, @Param("album") Album album);</code>
	 * 将解析出parsedKey.parameterNames=["album.id","owner.id"]，但对应的keyParameterIndexes就会是[1,0]
	 * </pre>
	 * 
	 */
	private Integer[] keyParameterIndexes;

	/**
	 * 解析后的vkey值对象
	 */
	private ParsedExpression parsedVkey;
	/**
	 * 用于从方法参数的Bean对象中获取需要的vkey的变量值的getter方法。缓存起来用于减少反射的查询操作
	 */
	private Method[] vkeyGetterMethods;
	/**
	 * parsedVkey的parameterNames中每个name对应对象在方法args[]数组中的索引值。
	 * 由于parameterNames包含"."，所以需要vkeyParameterIndexes记录"."之前的@Param对应的位置
	 * 
	 * <pre>
	 * 例如：
	 * <code>@CacheDelete(key="album-d-:albumId-:ownerId")</code>
	 * <code>@VerUpdate(vkey="ALBUM-:owner.id-:album.id-v")</code>
	 * public void updatePhoto(@Param("owner") User owner, @Param("album") Album album);</code>
	 * 将解析出parsedVkey.parameterNames=[owner.id"","album.id"]，但对应的keyParameterIndexes就会是[0,1]
	 * </pre>
	 * 
	 */
	private Integer[] vkeyParameterIndexes;

	/**
	 * 分页对象在方法args[]数组中的索引值
	 */
	private int pageIndex = -1;

	public ParsedExpression getParsedKey() {
		return parsedKey;
	}

	public Method[] getKeyGetterMethods() {
		return keyGetterMethods;
	}

	public Integer[] getKeyParameterIndexes() {
		return keyParameterIndexes;
	}

	public ParsedExpression getParsedVkey() {
		return parsedVkey;
	}

	public Method[] getVkeyGetterMethods() {
		return vkeyGetterMethods;
	}

	public Integer[] getVkeyParameterIndexes() {
		return vkeyParameterIndexes;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	/**
	 * 根据配置key，vkey，方法返回值，parameter的每个参数的类型，每个@Param参数在parameter数组中的位置
	 * 这些信息解析出parsedKey
	 * ，keyGetterMethods，keyParameterIndexes和parsedVkey，vkeyGetterMethods
	 * ，vkeyParameterIndexes
	 * 
	 * @param key
	 *            <code>@Cache</code>等中的原始key值
	 * @param vkey
	 *            <code>@Cache</code>等中的原始vkey值
	 * @param paramTypes
	 *            parameter的每个参数的类型
	 * @param paramIndexes
	 *            每个@Param参数在parameter数组中的位置，如@Param("user")在parameter[0]上，那么
	 *            paramIndexes.get("user")=0
	 * @param returnType
	 *            方法的返回类型
	 * @throws DaoGenerateException
	 */
	protected void configCacheKey(String key, String vkey, Class<?>[] paramTypes, Map<String, Integer> paramIndexes,
			Class<?> returnType) throws DaoGenerateException {
		// 获取Cache的表达式解析器
		ExpressionParser parser = ParserFacotory.createExpressionParser(ExpressionType.CACHE_KEY);
		// 由于@VerUpdate不存在key，就不需要生成parsedKey以及相关对象了
		if (StringUtils.isNotEmpty(key)) {
			parsedKey = parser.parse(key);
			List<String> keyParameterNames = parsedKey.getParameterNames();
			Object[] keyGettersAndIndexes = CommonUtils.getGettersAndIndexes(keyParameterNames, paramIndexes,
					paramTypes);
			keyGetterMethods = (Method[]) keyGettersAndIndexes[0];
			keyParameterIndexes = (Integer[]) keyGettersAndIndexes[1];
		}

		// 对于@CacheDelete不存在key且按照集合的方式处理，就不需要生成parsedVKey以及相关对象了
		// 对于没有@Cache，不论返回什么类型，都有可能没有配置vkey，这种情况下也不需要生成parsedVKey以及相关对象了
		if (StringUtils.isNotEmpty(vkey) && TypeUtils.isTypeCollection(returnType)) {
			// 集合类的版本号和普通的key生成类似，以参数中的@Param为准
			parsedVkey = parser.parse(vkey);
			List<String> vkeyParameterNames = parsedVkey.getParameterNames();
			Object[] vkeyGettersAndIndexes = CommonUtils.getGettersAndIndexes(vkeyParameterNames, paramIndexes,
					paramTypes);
			vkeyGetterMethods = (Method[]) vkeyGettersAndIndexes[0];
			vkeyParameterIndexes = (Integer[]) vkeyGettersAndIndexes[1];
		} else if (StringUtils.isNotEmpty(vkey)) {
			// 单个对象的返回值，特出处理，将返回值当做一个@Param("result")来处理
			parsedVkey = parser.parse(vkey);
			List<String> vkeyParameterNames = parsedVkey.getParameterNames();

			if (vkeyParameterNames.size() != 1 || !RESULT_PARAM_VALUE.equals(vkeyParameterNames.get(0))) {
				throw new DaoGenerateException("vkey in @Cache can only contain one parameter \"" + RESULT_PARAM_VALUE
						+ "\"");
			}

			Map<String, Integer> returnIndexes = new HashMap<String, Integer>(1);
			returnIndexes.put(RESULT_PARAM_VALUE, 0);
			Class<?>[] retTypes = new Class<?>[] { returnType };

			Object[] vkeyGettersAndIndexes = CommonUtils.getGettersAndIndexes(vkeyParameterNames, returnIndexes,
					retTypes);
			vkeyGetterMethods = (Method[]) vkeyGettersAndIndexes[0];
			vkeyParameterIndexes = (Integer[]) vkeyGettersAndIndexes[1];
		}
	}

	/**
	 * 方法
	 * {@link BaseCacheDefinition#configCacheKey(String, String, Class[], Map, Class)}
	 * 的默认调用方式。 用于@CacheDelete，@VerUpdate这类不需要考虑返回值的类型，对于这些类型默认以返回类型为集合处理。
	 * 
	 * @param key
	 *            <code>@Cache</code>等中的原始key值
	 * @param vkey
	 *            <code>@Cache</code>等中的原始vkey值
	 * @param paramTypes
	 *            parameter的每个参数的类型
	 * @param paramIndexes
	 *            每个@Param参数在parameter数组中的位置，如@Param("user")在parameter[0]上，那么
	 *            paramIndexes.get("user")=0
	 * @throws DaoGenerateException
	 */
	protected void configCacheKey(String key, String vkey, Class<?>[] paramTypes, Map<String, Integer> paramIndexes)
			throws DaoGenerateException {
		// 此方法需要按集合类处理vkey
		configCacheKey(key, vkey, paramTypes, paramIndexes, List.class);
	}

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
		Assert.notNull(parsedExpression, "parsedExpression can't be null while generate a key");

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

			Object value = CommonUtils.fetchVlaue(method, index, args, parameterNames);
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
		String oriKey = buildSimpleKey(args, parsedKey, keyGetterMethods, keyParameterIndexes);

		StringBuilder cacheKey = new StringBuilder();

		cacheKey.append(oriKey);

		Page page = CommonUtils.getPageArgument(args, pageIndex);
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
		String oriKey = buildSimpleKey(args, parsedVkey, vkeyGetterMethods, vkeyParameterIndexes);

		return oriKey;
	}

	/**
	 * 生成分页对象缓存的key值，即page-拼接上对象的缓存key值
	 * 
	 * @param args
	 * @return
	 */
	public String generatePageKey(Object[] args, String key) {
		Page page = CommonUtils.getPageArgument(args, pageIndex);
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
}
