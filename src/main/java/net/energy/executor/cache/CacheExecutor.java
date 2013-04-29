package net.energy.executor.cache;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

import net.energy.cache.Cache;
import net.energy.cache.CacheManager;
import net.energy.cache.CacheObject;
import net.energy.cache.CacheResult;
import net.energy.definition.cache.CacheDefinition;
import net.energy.definition.cache.CacheDefinitionCollection;
import net.energy.definition.cache.CacheDeleteDefinition;
import net.energy.definition.cache.VersionUpdateDefinition;
import net.energy.exception.CacheUnreachableException;
import net.energy.exception.DaoGenerateException;
import net.energy.executor.DataAccessExecutor;
import net.energy.utils.Page;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 缓存的访问操作调用器
 * 
 * @author wuqh
 * @see CacheDefinitionCollection
 */
public class CacheExecutor implements DataAccessExecutor {
	private static final Log LOGGER = LogFactory.getLog(CacheExecutor.class);
	private CacheDefinitionCollection cacheDefinitionCollection;
	private final CacheManager cacheManager;
	private DataAccessExecutor dataAccessExecutor;

	public CacheExecutor(CacheManager cacheManager, Method method) throws DaoGenerateException {
		this.cacheManager = cacheManager;
		this.cacheDefinitionCollection = new CacheDefinitionCollection(method);
	}

	public void setDataAccessExecutor(DataAccessExecutor dataAccessExecutor) {
		this.dataAccessExecutor = dataAccessExecutor;
	}

	@Override
	public Object execute(Object obj, Object[] args) {
		// 如果没有配置Cache操作，则直接执行数据访问操作
		if (!cacheDefinitionCollection.needCacheOpration()) {
			return dataAccessExecutor.execute(obj, args);
		}

		boolean dataAccessed = false;
		Object retVal = null;

		try {
			CacheResult cacheResult = beforeDataAccess(args);
			// 如果缓存命中
			if (cacheResult.isHit()) {
				return cacheResult.getRetVal();
			}

			retVal = dataAccessExecutor.execute(obj, args);
			dataAccessed = true;
			cacheResult.setRetVal(retVal);

			afterDataAccess(cacheResult, args);

			return retVal;
		} catch (CacheUnreachableException e) {
			if (!dataAccessed) {
				return dataAccessExecutor.execute(obj, args);
			} else {
				return retVal;
			}

		}

	}

	/**
	 * 访问数据前的操作：判断缓存是否存在，如果存在需要比较缓存的版本值和当前版本值是否一致
	 * 
	 * @param args
	 * @return
	 * @throws CacheUnreachableException
	 */
	private CacheResult beforeDataAccess(Object[] args) throws CacheUnreachableException {
		CacheResult cacheResult = new CacheResult(false);

		CacheDefinition cacheDefinition = cacheDefinitionCollection.getCacheDefinition();

		if (cacheDefinition == null) {
			return cacheResult;
		}

		// 生成缓存对象的key
		String key = cacheDefinition.generateCacheKey(args);
		if (StringUtils.isEmpty(key)) {
			LOGGER.debug("创建缓存Key失败,不进行缓存");
			return cacheResult;
		}
		cacheResult.setKey(key);

		// 获取缓存客户端
		Cache cache = cacheManager.getCache(cacheDefinition.getPool());
		cacheResult.setCache(cache);

		// 获取当前缓存的对象
		CacheObject oldItem = (CacheObject) cache.get(key);
		LOGGER.debug("从缓存中获取Key值[" + key + "]的对象[" + oldItem + "]");

		// 比较缓存版本，看缓存是否有效
		if (oldItem != null) {
			return compareVersionKey(cacheDefinition, cacheResult, oldItem, args);
		}

		return cacheResult;
	}

	/**
	 * 比较缓存版本，看缓存是否有效
	 * 
	 * @param cacheDefinition
	 * @param cacheResult
	 * @param oldItem
	 * @param args
	 * @return
	 */
	private CacheResult compareVersionKey(CacheDefinition cacheDefinition, CacheResult cacheResult,
			CacheObject oldItem, Object[] args) {
		long itemVersion = oldItem.getVersion();
		Object cachedItem = oldItem.getCacheObject();

		Object[] params;
		boolean isReturnCollection = cacheDefinition.isReturnCollection();
		// 通过判断缓存的是单个对象还是集合，然后根据@Cache的vkey值来生成对应的版本Key值：
		// 1、集合通过方法传入中的@Param对应的值来生成；
		// 2、如果是单个集合通过"result"关键字来生成版本号。
		if (isReturnCollection) {
			params = args;
		} else {// 单个对象
			// 缓存对象为空或者不需要判断versionKey就作为命中处理
			if (cachedItem == null || StringUtils.isEmpty(cacheDefinition.getVkey())) {
				return processCacheHit(cacheResult, cachedItem, args);
			}
			// 因为将单个对象关联vkey时就好像在返回接口上加了一个@Param("result")
			// 所以需要获取单个对象的版本key时需要获取缓存的对象，并将对象作为生成版本key的参数来使用
			params = new Object[] { cachedItem };
		}

		String versionKey = cacheDefinition.generateVersionKey(params);
		if (StringUtils.isEmpty(versionKey)) {
			LOGGER.debug("无法生成更新版本信息的缓存Key,获取缓存失败");
			return cacheResult;
		}
		LOGGER.debug("生成版本信息的缓存Key[" + versionKey + "]");
		cacheResult.setVersionKey(versionKey);

		Cache cache = cacheResult.getCache();
		// 根据版本的key值，获取当前版本的值
		long currentVersion = getCurrentVersion(cache, versionKey);
		cacheResult.setCurrentVersion(currentVersion);

		// 如果当前版本值，和缓存中设置的值一致，说明这段时间没有更新，缓存有效。
		if (currentVersion != 0 && currentVersion == itemVersion) {
			return processCacheHit(cacheResult, cachedItem, args);
		} else {
			LOGGER.debug("当前版本为[" + currentVersion + "],缓存对象版本号为[" + itemVersion + "],Cache没有命中,直接调用DAO方法");
			return cacheResult;
		}
	}

	/**
	 * 根据版本的key值，获取版本号
	 * 
	 * @param cache
	 * @param versionKey
	 * @return
	 */
	private long getCurrentVersion(Cache cache, String versionKey) {
		Long cachedVersion = (Long) cache.get(versionKey);
		LOGGER.debug("获取Key值为[" + versionKey + "]版本信息,版本号[" + cachedVersion + "]");
		long currentVersion = 0;
		if (cachedVersion != null) {
			currentVersion = cachedVersion;
		}
		return currentVersion;
	}

	/**
	 * 缓存命中后的处理：缓存命中后需要判断是否为分页的数据，如果是分页查询的还需要取出缓存的分页数据（总记录数）
	 * 
	 * @param cacheResult
	 * @param cachedItem
	 * @param args
	 * @return
	 */
	private CacheResult processCacheHit(CacheResult cacheResult, Object cachedItem, Object[] args) {
		LOGGER.debug("版本号一致，缓存命中");

		String key = cacheResult.getKey();
		cacheResult.setRetVal(cachedItem);

		CacheDefinition cacheDefinition = cacheDefinitionCollection.getCacheDefinition();
		Cache cache = cacheResult.getCache();

		// 判断是否存在分页，如果分页了还需要特殊处理
		if (cacheDefinition.isReturnCollection()) {
			Page page = cacheDefinition.getPageArgument(args);
			// 如果分页对象为空，表示此分页被复用与不分页的情况了，需要加以区分
			if (page != null) {
				String pageKey = cacheDefinition.generatePageKey(args, key);
				LOGGER.debug("缓存为需要分页的缓存，页码[" + page.getCurpage() + "],对应的分页缓存Key为[" + pageKey + "]");
				Page cachePage = (Page) cache.get(pageKey);
				// 如果取不到分页数据，需要当做未命中处理
				if (cachePage == null) {
					cacheResult.setHit(false);
					return cacheResult;
				}
				page.setTotal(cachePage.getTotal());
				cacheResult.setPageKey(pageKey);
			}
		}
		cacheResult.setHit(true);
		return cacheResult;
	}

	/**
	 * 调用数据访问后的处理：缓存查询后的对象，更新版本号，删除缓存对象
	 * 
	 * @param cacheResult
	 * @param args
	 * @throws CacheUnreachableException
	 */
	private void afterDataAccess(CacheResult cacheResult, Object[] args) throws CacheUnreachableException {
		CacheDefinition cacheDefinition = cacheDefinitionCollection.getCacheDefinition();
		// 如果不是缓存获取操作，则只需要进行简单的缓存删除和版本信息更新
		if (cacheDefinition == null) {
			updateVersion(cacheResult, args);
			deleteCache(cacheResult, args);
			return;
		}
		// 对于没有命中的缓存，需要进行缓存的更新以及版本信息的重新构建。
		String key = cacheResult.getKey();
		Object exeResult = cacheResult.getRetVal();
		Cache cache = cacheResult.getCache();
		// 版本信息的重新构建
		cacheResult = recacheVersionKey(cacheResult, args, cacheDefinition);

		long expire = cacheDefinition.getExpire();

		long currentVersion = cacheResult.getCurrentVersion();
		// 缓存查询结果
		CacheObject newItem = new CacheObject((Serializable) exeResult, currentVersion);
		cache.add(key, newItem, expire);
		LOGGER.debug("缓存对象到[" + key + "],缓存时间[" + expire + "]毫秒");
		// 如果是分页查询还需要缓存分页相关信息
		if (cacheDefinition.isReturnCollection()) {
			Page page = cacheDefinition.getPageArgument(args);
			// 如果分页对象为空，表示此分页被复用与不分页的情况了，需要加以区分
			if (page != null) {
				String pageKey = cacheResult.getPageKey();
				// 如果在beforeDataAccess执行过程中已经产生了pageKey就不需要重复生成了
				if (pageKey == null) {
					pageKey = cacheDefinition.generatePageKey(args, key);
				}
				if (!StringUtils.isEmpty(pageKey)) {
					cache.add(pageKey, page, expire);
					LOGGER.debug("缓存为需要分页的缓存,页码[" + page.getCurpage() + "],缓存分页对象到[" + pageKey + "], 缓存时间[" + expire
							+ "]毫秒");
				}
			}
		}

	}

	/**
	 * 更新指定版本的当前版本号
	 * 
	 * @param cacheResult
	 * @param args
	 * @throws CacheUnreachableException
	 */
	private void updateVersion(CacheResult cacheResult, Object[] args) throws CacheUnreachableException {
		List<VersionUpdateDefinition> updates = cacheDefinitionCollection.getVersionUpdateDefinitions();
		if (updates == null) {
			return;
		}

		// 根据cacheDefinitionCollection中配置的所有更新版本操作，循环调用，更新版本号
		for (VersionUpdateDefinition definition : updates) {
			String versionKey = definition.generateVersionKey(args);
			if (versionKey == null) {
				LOGGER.info("无法生成更新版本信息缓存的Key,更新缓存版本失败");
			}
			// 使用系统时间作为版本号，防止当前版本号与之前某个版本号正好一致，而是对象无法失效
			long currentVersion = System.currentTimeMillis();
			if (versionKey != null) {
				Cache cache = cacheManager.getCache(definition.getPool());
				long expire = definition.getExpire();
				cache.add(versionKey, currentVersion, expire);
				LOGGER.debug("更新版本信息,版本Key值[" + versionKey + "],版本号[" + currentVersion + "], 版本信息缓存时间[" + expire
						+ "]毫秒");
			}
		}
	}

	/**
	 * 
	 * 参数指定缓存
	 * 
	 * @param cacheResult
	 * @param args
	 * @throws CacheUnreachableException
	 */
	private void deleteCache(CacheResult cacheResult, Object[] args) throws CacheUnreachableException {
		List<CacheDeleteDefinition> deletes = cacheDefinitionCollection.getCacheDeleteDefinitions();
		if (deletes == null) {
			return;
		}
		// 根据cacheDefinitionCollection中配置的所有删除缓存操作，循环调用，删除缓存
		for (CacheDeleteDefinition definition : deletes) {
			String key = definition.generateCacheKey(args);
			Cache cache = cacheManager.getCache(definition.getPool());
			cache.delete(key);
			LOGGER.debug("删除Key为[" + key + "]的缓存");
		}
	}

	/**
	 * 对于判断为缓存失效的，需要在查询成功后重建版本信息。
	 * 
	 * @param cacheResult
	 * @param args
	 * @param cacheDefinition
	 * @return
	 */
	private CacheResult recacheVersionKey(CacheResult cacheResult, Object[] args, CacheDefinition cacheDefinition) {
		long currentVersion = cacheResult.getCurrentVersion();
		String versionKey = cacheResult.getVersionKey();
		boolean isReturnCollection = cacheDefinition.isReturnCollection();
		Cache cache = cacheResult.getCache();
		Object result = cacheResult.getRetVal();

		// 原来的version已经因为时间等原因过期，需要重新缓存。
		// 否则就不需要重新缓存，直接以当前的版本号作为缓存对象的版本号
		if (currentVersion <= 0) {
			Long cachedCurrentVersion = null;
			// versionKey为空表示beforeDataAccess执行过程中并没有到compareVersionKey这一步。
			// 可以认为是第一次查询缓存，需要生产版本的key，来获取当前版本信息;
			if (versionKey == null) {
				if (isReturnCollection) {
					versionKey = cacheDefinition.generateVersionKey(args);
				} else if (result != null && StringUtils.isNotEmpty(cacheDefinition.getVkey())) {
					versionKey = cacheDefinition.generateVersionKey(new Object[] { result });
				} else {// 如果返回值是单个的Null对象就不需要也无法关联版本
					LOGGER.info("不需要缓存版本信息");
					return cacheResult;
				}

				if (versionKey == null) {
					LOGGER.info("无法生成更新版本信息的缓存Key,缓存版本信息失败");
					return cacheResult;
				}
				LOGGER.debug("第一次查询缓存，需要生成版本信息,版本信息缓存的Key[" + versionKey + "]");
				// 由于第一次查询无法得知版本信息，所以在知道版本key后需要再查一次版本信息。
				cachedCurrentVersion = (Long) cache.get(versionKey);

			}

			// versionKey不为空但是对应的值却为空或者无意义，表明版本信息不存在，需要更新版本信息。
			// 否则就可以将缓存的版本信息作为当前的版本信息
			if (versionKey != null && (cachedCurrentVersion == null || cachedCurrentVersion <= 0)) {
				// 使用系统时间作为版本号，防止当前版本号与之前某个版本号正好一致，而是对象无法失效
				currentVersion = System.currentTimeMillis();
				long expire = cacheDefinition.getExpire();
				cache.add(versionKey, currentVersion, expire);

				LOGGER.debug("更新版本信息，版本Key值[" + versionKey + "],版本号[" + currentVersion + "], 版本信息缓存时间[" + expire
						+ "]毫秒");
				cacheResult.setCurrentVersion(currentVersion);
			} else {
				// 如果versionKey为空，说明缓存不需要关联版本信息，直接以当前时间作为缓存版本的版本号
				if (cachedCurrentVersion == null || cachedCurrentVersion <= 0) {
					// 使用系统时间作为版本号，防止当前版本号与之前某个版本号正好一致，而是对象无法失效
					cachedCurrentVersion = System.currentTimeMillis();
				}
				cacheResult.setCurrentVersion(cachedCurrentVersion);
			}
		}

		return cacheResult;
	}
}
