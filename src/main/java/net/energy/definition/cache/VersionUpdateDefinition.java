package net.energy.definition.cache;

import java.lang.reflect.Method;

import net.energy.annotation.cache.VerUpdate;
import net.energy.exception.DaoGenerateException;
import net.energy.expression.ParsedExpression;

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
	private final String pool;
	/**
	 * 原始的vkey值
	 */
	private final String vkey;
	/**
	 * 版本缓存最大生存时间，单位：毫秒
	 */
	private final long expire;

	public VersionUpdateDefinition(VerUpdate update, Method method) throws DaoGenerateException {
		//VerUpdate信息获取必须放在initDefinition之前，以免调用getSourceKey时无法获取key值
		pool = update.pool();
		expire = update.expire();
		vkey = update.vkey();
		
		//特别注释：VersionUpdateDefinition不需要调用getVkeyGettersAndIndexes来生成
		initDefinition(method);
	}

	@Override
	protected String getSourceKey() {
		return vkey;
	}
	
	public String getPool() {
		return pool;
	}

	public long getExpire() {
		return expire;
	}
	
	public String getVkey() {
		return vkey;
	}

	@Override
	public ParsedExpression getParsedKey() {
		throw new UnsupportedOperationException("VersionUpdateDefinition不支持调用此方法");
	}

	@Override
	public Method[] getKeyGetterMethods() {
		throw new UnsupportedOperationException("VersionUpdateDefinition不支持调用此方法");
	}

	@Override
	public Integer[] getKeyParameterIndexes() {
		throw new UnsupportedOperationException("VersionUpdateDefinition不支持调用此方法");
	}

	@Override
	public ParsedExpression getParsedVkey() {
		return getParsedExpression();
	}

	@Override
	public Method[] getVkeyGetterMethods() {
		return getGetterMethods();
	}

	@Override
	public Integer[] getVkeyParameterIndexes() {
		return getParameterIndexes();
	}

}
