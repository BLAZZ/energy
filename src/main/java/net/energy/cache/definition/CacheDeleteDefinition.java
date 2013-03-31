package net.energy.cache.definition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.energy.annotation.cache.CacheDelete;
import net.energy.exception.DaoGenerateException;

/**
 * 通过对配置了@CacheDelete的方法的解析，产生需要在执行cache操作时必要用到的参数。
 * 
 * @author wuqh
 * 
 */
public class CacheDeleteDefinition extends BaseCacheDefinition {
	/**
	 * 缓存的pool
	 */
	private String pool;
	/**
	 * 原始的key值
	 */
	private String key;

	public CacheDeleteDefinition(CacheDelete cacheDelete, Method method) throws DaoGenerateException {
		init(cacheDelete, method);
	}

	protected void init(CacheDelete cacheDelete, Method method) throws DaoGenerateException {
		pool = cacheDelete.pool();
		key = cacheDelete.key();

		Class<?>[] paramTypes = method.getParameterTypes();
		Annotation[][] annotations = method.getParameterAnnotations();
		Map<String, Integer> paramIndexes = new HashMap<String, Integer>(8, 1f);
		parseParameterAnnotations(annotations, paramTypes, paramIndexes);

		configCacheKey(cacheDelete.key(), null, paramTypes, paramIndexes);

	}

	public String getPool() {
		return pool;
	}

	public void setPool(String pool) {
		this.pool = pool;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
