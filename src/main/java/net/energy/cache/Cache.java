package net.energy.cache;

/**
 * Cache封装接口，操作过程中如果无法正常操作，将抛出CacheUnreachableException
 * 
 * @author wuqh
 */
public interface Cache {
	/**
	 * 把对象存放到Cache，如果Cache中存在该Key对应对象，则替换，过期时间等使用系统默认配置
	 * 
	 * @param key
	 * @param value
	 */
	void add(final String key, final Object value);

	/**
	 * 把对象存放到Cache，如果Cache中存在该Key对应对象，则替换，并设置过期时间（过期时间为对象存活的最长时间，单位：毫秒）
	 * 
	 * @param key
	 * @param value
	 * @param expiry
	 */
	void add(final String key, final Object value, final long expiry);

	/**
	 * 把对象从Cache中删除
	 * 
	 * @param key
	 * @return 删除是否成功
	 */
	boolean delete(final String key);

	/**
	 * 删除Cache中的对象，请谨慎使用
	 */
	void removeAll();

	/**
	 * 获取Cache中的对象
	 * 
	 * @param key
	 * @return 获取cache对象,或不存在返回Null
	 */
	Object get(final String key);

	/**
	 * Checks to see if key exists in cache.
	 * 
	 * @param key
	 * @return 返回对应key是否存在且有key
	 */
	boolean keyExists(final String key);

}
