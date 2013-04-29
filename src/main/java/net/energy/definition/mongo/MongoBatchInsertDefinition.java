package net.energy.definition.mongo;

import java.lang.reflect.Method;

import net.energy.annotation.mongo.MongoBatchInsert;
import net.energy.definition.BatchDefinition;
import net.energy.exception.DaoGenerateException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 通过对配置了@MongoBatchInsert方法的解析， 产生需要在执行Mongo批量插入操作时必要用到的参数。
 * 
 * @author wuqh
 * 
 */
public class MongoBatchInsertDefinition extends BaseMongoDefinition implements BatchDefinition {
	private static final Log LOGGER = LogFactory.getLog(MongoBatchInsertDefinition.class);

	public MongoBatchInsertDefinition(Method method) throws DaoGenerateException {
		super(method);
	}

	@Override
	protected String getSourceShell(Method method) {
		MongoBatchInsert insertShell = method.getAnnotation(MongoBatchInsert.class);
		return insertShell.value();
	}

	@Override
	protected void checkBeforeParse(Method method) throws DaoGenerateException {
		super.checkBeforeParse(method);
		checkUpsetReturnType(method, "MongoBatchInsert");
	}

	@Override
	protected void logBindInfo(Method method) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("绑定" + getDescription() + "到方法[" + method + "]成功");
		}
	}

	private String getDescription() {
		String desc = "@MongoBatchInsert(" + this.getParsedShell().getOriginalExpression() + ")";

		if (!StringUtils.isEmpty(globalCollectionName)) {
			desc = ",@MongoCollection(" + globalCollectionName + ")";
		}

		return desc;
	}

}
