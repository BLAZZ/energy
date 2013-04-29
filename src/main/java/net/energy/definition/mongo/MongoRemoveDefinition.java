package net.energy.definition.mongo;

import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.energy.annotation.mongo.MongoRemove;
import net.energy.exception.DaoGenerateException;

/**
 * 通过对配置了@MongoRemove方法的解析， 产生需要在执行Mongo删除操作时必要用到的参数。
 * 
 * @author wuqh
 * 
 */
public class MongoRemoveDefinition extends BaseMongoDefinition {
	private static final Log LOGGER = LogFactory.getLog(MongoRemoveDefinition.class);
	
	public MongoRemoveDefinition(Method method) throws DaoGenerateException {
		super(method);
	}

	@Override
	protected String getSourceShell(Method method) {
		MongoRemove rmShell = method.getAnnotation(MongoRemove.class);
		return rmShell.value();
	}
	
	@Override
	protected void checkBeforeParse(Method method) throws DaoGenerateException {
		super.checkBeforeParse(method);
		checkUpsetReturnType(method, "MongoRemove");
	}

	@Override
	protected void logBindInfo(Method method) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("绑定" + getDescription() + "到方法[" + method + "]成功");
		}
	}
	
	private String getDescription() {
		String desc = "@MongoRemove(" + this.getParsedShell().getOriginalExpression() + ")";

		if (!StringUtils.isEmpty(globalCollectionName)) {
			desc = ",@MongoCollection(" + globalCollectionName + ")";
		}
		
		return desc;
	}
}
