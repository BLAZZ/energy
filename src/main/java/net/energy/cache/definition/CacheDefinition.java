package net.energy.cache.definition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import net.energy.annotation.cache.Cache;
import net.energy.exception.DaoGenerateException;
import net.energy.utils.TypeUtils;

/**
 * 通过对配置了@Cache的方法的解析，产生需要在执行cache操作时必要用到的参数。
 * 
 * @author wuqh
 * 
 */
public class CacheDefinition extends BaseCacheDefinition {
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
		init(cache, method);
	}

	private void init(Cache cache, Method method) throws DaoGenerateException {
		pool = cache.pool();
		expire = cache.expire();
		key = cache.key();
		vkey = cache.vkey();

		Class<?>[] paramTypes = method.getParameterTypes();
		Annotation[][] annotations = method.getParameterAnnotations();
		Map<String, Integer> paramIndexes = new HashMap<String, Integer>(8, 1f);
		parseParameterAnnotations(annotations, paramTypes, paramIndexes);

		Class<?> returnType = method.getReturnType();

		configCacheKey(cache.key(), cache.vkey(), paramTypes, paramIndexes, returnType);

		checkConfig(returnType);
	}

	/**
	 * 对于@Cache接口一些约定规则的检查。
	 * 
	 * @param returnType
	 * @throws DaoGenerateException
	 */
	private void checkConfig(Class<?> returnType) throws DaoGenerateException {
		isReturnCollection = TypeUtils.isTypeCollection(returnType);
		if (isReturnCollection && StringUtils.isEmpty(vkey)) {
			throw new DaoGenerateException("need vkey in @Cache if returns collection");
		}
	}

	public String getPool() {
		return pool;
	}

	public String getKey() {
		return key;
	}

	public String getVkey() {
		return vkey;
	}

	public long getExpire() {
		return expire;
	}

	public boolean isReturnCollection() {
		return isReturnCollection;
	}

}
