package net.energy.definition.mongo;

import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.energy.annotation.mongo.MongoCount;
import net.energy.exception.DaoGenerateException;
import net.energy.utils.EnergyClassUtils;

/**
 * 通过对配置了@MongoCount方法的解析， 产生需要在执行Mongo统计操作时必要用到的参数。
 * 
 * @author wuqh
 * 
 */
public class MongoCountDefinition extends BaseMongoDefinition {
	private static final Log LOGGER = LogFactory.getLog(MongoCountDefinition.class);
	
	public MongoCountDefinition(Method method) throws DaoGenerateException {
		super(method);
	}

	@Override
	protected String getSourceShell(Method method) {
		MongoCount countShell = method.getAnnotation(MongoCount.class);
		return countShell.value();
	}
	
	@Override
	protected void checkBeforeParse(Method method) throws DaoGenerateException {
		super.checkBeforeParse(method);
		checkReturnType(method);
	}

	/**
	 * 检测返回值，返回值必须是int或其装箱类型
	 * 
	 * @param method
	 * @throws DaoGenerateException
	 */
	private void checkReturnType(Method method) throws DaoGenerateException {
		Class<?> returnType = method.getReturnType();

		if (EnergyClassUtils.isTypePrimitive(returnType)) {
			returnType = EnergyClassUtils.primitiveToWrapper(returnType);
		}
		if (Integer.class.equals(returnType)) {
			return;
		}

		throw new DaoGenerateException("方法[" + method + "]配置错误：配置了@MongoCount注解的方法只能返回int或者Integer");

	}


	@Override
	protected void logBindInfo(Method method) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("绑定" + getDescription() + "到方法[" + method + "]成功");
		}
	}
	
	private String getDescription() {
		String desc = "@MongoCount(" + this.getParsedShell().getOriginalExpression() + ")";

		if (!StringUtils.isEmpty(globalCollectionName)) {
			desc = ",@MongoCollection(" + globalCollectionName + ")";
		}
		
		return desc;
	}
}
