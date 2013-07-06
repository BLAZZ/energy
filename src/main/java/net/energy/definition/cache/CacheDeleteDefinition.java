package net.energy.definition.cache;

import java.lang.reflect.Method;

import net.energy.annotation.cache.CacheDelete;
import net.energy.exception.DaoGenerateException;
import net.energy.expression.ParsedExpression;

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
	private final String pool;
	/**
	 * 原始的key值
	 */
	private final String key;

	public CacheDeleteDefinition(CacheDelete cacheDelete, Method method) throws DaoGenerateException {
		//CacheDelete信息获取必须放在initDefinition之前，以免调用getSourceKey时无法获取key值
		pool = cacheDelete.pool();
		key = cacheDelete.key();
		
		initDefinition(method);
	}

	@Override
	protected String getSourceKey() {
		return key;
	}
	
	public String getPool() {
		return pool;
	}
	
	public String getKey() {
		return key;
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
		throw new UnsupportedOperationException("CacheDeleteDefinition不支持调用此方法");
	}

	@Override
	public Method[] getVkeyGetterMethods() {
		throw new UnsupportedOperationException("CacheDeleteDefinition不支持调用此方法");
	}

	@Override
	public Integer[] getVkeyParameterIndexes() {
		throw new UnsupportedOperationException("CacheDeleteDefinition不支持调用此方法");
	}

}
