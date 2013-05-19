package net.energy.definition.cache;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.energy.annotation.cache.Cache;
import net.energy.exception.DaoGenerateException;
import net.energy.expression.ExpressionParser;
import net.energy.expression.ParsedExpression;
import net.energy.expression.ParserFacotory;
import net.energy.expression.ParserFacotory.ExpressionType;
import net.energy.utils.ClassHelper;
import net.energy.utils.ReflectionUtils;

import org.apache.commons.lang.StringUtils;

/**
 * 通过对配置了@Cache的方法的解析，产生需要在执行cache操作时必要用到的参数。
 * 
 * @author wuqh
 * 
 */
public class CacheDefinition extends BaseCacheDefinition {

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
	 * 缓存的pool
	 */
	private String pool;
	/**
	 * 原始的key值
	 */
	private String key;
	/**
	 * 原始的vkey值
	 */
	private String vkey;
	/**
	 * 缓存、版本缓存最大生存时间，单位：毫秒
	 */
	private long expire;

	/**
	 * 返回类型是否为集合
	 */
	private boolean isReturnCollection;

	public CacheDefinition(Cache cache, Method method) throws DaoGenerateException {
		// Cache信息获取必须放在initDefinition之前，以免调用getSourceKey时无法获取key值
		pool = cache.pool();
		expire = cache.expire();
		key = cache.key();
		vkey = cache.vkey();

		initDefinition(method);
	}

	@Override
	protected String getSourceKey(Method method) {
		return key;
	}

	@Override
	protected void parseInternal(Method method, Map<String, Integer> paramIndexes,
			Map<String, Integer> batchParamIndexes) throws DaoGenerateException {
		super.parseInternal(method, paramIndexes, batchParamIndexes);

		// 解析Vkey的相关表达式级参数信息
		if (StringUtils.isNotEmpty(vkey)) {// 对于单个缓存对象，可以允许不设置vkey
			ExpressionParser parser = ParserFacotory.createExpressionParser(ExpressionType.CACHE_KEY);
			parsedVkey = parser.parse(vkey);
			List<String> vkeyParameterNames = parsedVkey.getParameterNames();
			Object[] vkeyGettersAndIndexes = getVkeyGettersAndIndexes(method, vkeyParameterNames, paramIndexes,
					batchParamIndexes);
			vkeyGetterMethods = (Method[]) vkeyGettersAndIndexes[0];
			vkeyParameterIndexes = (Integer[]) vkeyGettersAndIndexes[1];
		}
	}

	/**
	 * Vkey的GettersAndIndexes需要根据返回值类型进行区分
	 * 
	 * @param method
	 * @param vkeyParameterNames
	 * @param paramIndexes
	 * @param batchParamIndexes
	 * @return
	 * @throws DaoGenerateException
	 */
	protected Object[] getVkeyGettersAndIndexes(Method method, List<String> vkeyParameterNames,
			Map<String, Integer> paramIndexes, Map<String, Integer> batchParamIndexes) throws DaoGenerateException {

		Class<?> returnType = method.getReturnType();

		if (ClassHelper.isTypeCollection(returnType)) {
			// 集合类的版本号和普通的key生成类似，以父类实现为准为准
			return super.getGettersAndIndexes(method, vkeyParameterNames, paramIndexes, batchParamIndexes);
		} else {
			// 单个对象的返回值，特出处理，将返回值当做一个@Param("result")来处理

			if (vkeyParameterNames.size() != 1 || !RESULT_PARAM_VALUE.equals(vkeyParameterNames.get(0))) {
				throw new DaoGenerateException("方法[" + method + "]配置错误： 对于返回非集合类型的方法，@Cache注解的vkey有且只能有一个参数\""
						+ RESULT_PARAM_VALUE + "\"");
			}

			Map<String, Integer> returnIndexes = new HashMap<String, Integer>(1);
			returnIndexes.put(RESULT_PARAM_VALUE, 0);
			Class<?>[] retTypes = new Class<?>[] { returnType };

			return ReflectionUtils.getGettersAndIndexes(vkeyParameterNames, returnIndexes, retTypes);
		}
	}

	@Override
	protected void checkAfterParse(Method method) throws DaoGenerateException {
		super.checkAfterParse(method);

		Class<?> returnType = method.getReturnType();
		isReturnCollection = ClassHelper.isTypeCollection(returnType);
		if (isReturnCollection && StringUtils.isEmpty(vkey)) {
			throw new DaoGenerateException("方法[" + method + "]配置错误： 对于返回集合类型的方法，@Cache注解中必须包含");
		}
	}

	public String getPool() {
		return pool;
	}

	public long getExpire() {
		return expire;
	}

	public boolean isReturnCollection() {
		return isReturnCollection;
	}

	public String getKey() {
		return key;
	}

	public String getVkey() {
		return vkey;
	}

	@Override
	public ParsedExpression getParsedKey() {
		return getParsedExpression();
	}

	@Override
	public Method[] getKeyGetterMethods() {
		return getGetterMethods();
	}

	@Override
	public Integer[] getKeyParameterIndexes() {
		return getParameterIndexes();
	}

	@Override
	public ParsedExpression getParsedVkey() {
		return this.parsedVkey;
	}

	@Override
	public Method[] getVkeyGetterMethods() {
		return this.vkeyGetterMethods;
	}

	@Override
	public Integer[] getVkeyParameterIndexes() {
		return this.vkeyParameterIndexes;
	}

}
