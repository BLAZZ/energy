package net.energy.cache.definition;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.energy.annotation.cache.Cache;
import net.energy.annotation.cache.CacheDelete;
import net.energy.annotation.cache.CacheUpdate;
import net.energy.annotation.cache.VerUpdate;
import net.energy.exception.DaoGenerateException;

/**
 * 单个方法中所有Cache相关的配置。 由于{@link CacheUpdate}等更新、操作集的存在。其实一次方法的执行会包含： 1、一个缓存查询；
 * 2、N个缓存删除； 3、N个缓存版本更新； 因而，CacheDefinitionCollection就被定义为包含这些配置信息的集合。
 * 
 * @author wuqh
 * 
 */
public class CacheDefinitionCollection {
	private static final Log LOGGER = LogFactory.getLog(CacheDefinitionCollection.class);
	private CacheDefinition cacheDefinition = null;
	private List<CacheDeleteDefinition> cacheDeleteDefinitions = null;
	private List<VersionUpdateDefinition> versionUpdateDefinitions = null;

	public CacheDefinitionCollection(Method method) throws DaoGenerateException {

		Cache cache = method.getAnnotation(Cache.class);
		if (cache != null) {
			cacheDefinition = new CacheDefinition(cache, method);
		}

		CacheDelete cacheDelete = method.getAnnotation(CacheDelete.class);
		if (cacheDelete != null) {
			CacheDeleteDefinition definition = new CacheDeleteDefinition(cacheDelete, method);
			cacheDeleteDefinitions = lazyInit(cacheDeleteDefinitions);
			cacheDeleteDefinitions.add(definition);
		}

		VerUpdate versionUpdate = method.getAnnotation(VerUpdate.class);
		if (versionUpdate != null) {
			VersionUpdateDefinition definition = new VersionUpdateDefinition(versionUpdate, method);
			versionUpdateDefinitions = lazyInit(versionUpdateDefinitions);
			versionUpdateDefinitions.add(definition);
		}

		CacheUpdate cacheUpdate = method.getAnnotation(CacheUpdate.class);
		if (cacheUpdate != null) {
			parseCacheUpdateDeiniftion(cacheUpdate, method);
		}

		if (cacheDefinition != null && (cacheDeleteDefinitions != null || versionUpdateDefinitions != null)) {
			LOGGER.info("@Cache can't be used with other Cache Annotation, and it will be not effective");
			cacheDefinition = null;
		}
	}

	public boolean needCacheOpration() {
		return (cacheDefinition != null || cacheDeleteDefinitions != null || versionUpdateDefinitions != null);
	}

	public CacheDefinition getCacheDefinition() {
		return cacheDefinition;
	}

	public List<CacheDeleteDefinition> getCacheDeleteDefinitions() {
		return cacheDeleteDefinitions;
	}

	public List<VersionUpdateDefinition> getVersionUpdateDefinitions() {
		return versionUpdateDefinitions;
	}

	private <T> List<T> lazyInit(List<T> list) {
		if (list == null) {
			return new ArrayList<T>(1);
		} else {
			return list;
		}
	}

	private void parseCacheUpdateDeiniftion(CacheUpdate cacheUpdate, Method method) throws DaoGenerateException {
		CacheDelete[] deletes = cacheUpdate.delete();
		VerUpdate[] updates = cacheUpdate.update();

		for (CacheDelete delete : deletes) {
			CacheDeleteDefinition definition = new CacheDeleteDefinition(delete, method);
			cacheDeleteDefinitions = lazyInit(cacheDeleteDefinitions);
			cacheDeleteDefinitions.add(definition);
		}

		for (VerUpdate update : updates) {
			VersionUpdateDefinition definition = new VersionUpdateDefinition(update, method);
			versionUpdateDefinitions = lazyInit(versionUpdateDefinitions);
			versionUpdateDefinitions.add(definition);
		}
	}

}
