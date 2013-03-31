package net.energy.cache.definition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.energy.annotation.cache.VerUpdate;
import net.energy.exception.DaoGenerateException;

/**
 * 通过对配置了@VerUpdate的方法的解析，产生需要在执行cache操作时必要用到的参数。
 * 
 * @author wuqh
 * 
 */
public class VersionUpdateDefinition extends BaseCacheDefinition {
	/**
	 * 缓存的pool
	 */
	private String pool;
	/**
	 * 原始的vkey值
	 */
	private String vkey;
	/**
	 * 版本缓存最大生存时间，单位：毫秒
	 */
	private long expire;

	public VersionUpdateDefinition(VerUpdate update, Method method) throws DaoGenerateException {
		init(update, method);
	}

	private void init(VerUpdate update, Method method) throws DaoGenerateException {
		pool = update.pool();
		expire = update.expire();
		vkey = update.vkey();

		Class<?>[] paramTypes = method.getParameterTypes();
		Annotation[][] annotations = method.getParameterAnnotations();
		Map<String, Integer> paramIndexes = new HashMap<String, Integer>(8, 1f);
		parseParameterAnnotations(annotations, paramTypes, paramIndexes);

		configCacheKey(null, update.vkey(), paramTypes, paramIndexes);

	}

	public String getPool() {
		return pool;
	}

	public void setPool(String pool) {
		this.pool = pool;
	}

	public String getVkey() {
		return vkey;
	}

	public void setVkey(String vkey) {
		this.vkey = vkey;
	}

	public long getExpire() {
		return expire;
	}

	public void setExpire(long expire) {
		this.expire = expire;
	}

}
