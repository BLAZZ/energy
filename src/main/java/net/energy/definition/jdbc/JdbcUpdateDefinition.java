package net.energy.definition.jdbc;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.energy.annotation.ReturnId;
import net.energy.annotation.jdbc.Update;
import net.energy.exception.DaoGenerateException;
import net.energy.utils.EnergyClassUtils;


/**
 * 通过对配置了@Update的方法的解析，产生需要在执行JDBC操作时必要用到的参数。
 * 
 * @author wuqh
 * 
 */
public class JdbcUpdateDefinition extends BaseJdbcDefinition {
	private static final Log LOGGER = LogFactory.getLog(JdbcUpdateDefinition.class);
	private boolean isReturnId = false;

	public boolean isReturnId() {
		return isReturnId;
	}

	public JdbcUpdateDefinition(Method method) throws DaoGenerateException {
		super(method);
	}

	protected String getSourceSql(Method method) {
		// 解析修改的SQL语句
		Update update = method.getAnnotation(Update.class);
		return update.value();
	}
	
	@Override
	protected void checkBeforeParse(Method method) throws DaoGenerateException {
		super.checkBeforeParse(method);
		checkReturnType(method);
	}

	/**
	 * 检查加了@Update注解的方法的返回值类型。规则： 如果有@ReturnId就必须是Number的父类或者基本类型
	 * 如果没有@ReturnId就必须返回void、int或者Integer
	 * 
	 * @param method
	 * @throws DaoGenerateException
	 */
	private void checkReturnType(Method method) throws DaoGenerateException {
		Class<?> returnType = method.getReturnType();
		isReturnId = (method.getAnnotation(ReturnId.class) != null);

		if (isReturnId) {
			if (EnergyClassUtils.isTypePrimitive(returnType)) {
				returnType = EnergyClassUtils.primitiveToWrapper(returnType);
			}
			if (!EnergyClassUtils.isTypeNumber(returnType)) {
				throw new DaoGenerateException("方法[" + method + "]配置错误：配置了@ReturnId注解的方法只能返回java.lang.Number类型数据或者对应的基本类型，你能为[" + returnType + "]");
			}
		} else {
			if (EnergyClassUtils.isTypePrimitive(returnType)) {
				returnType = EnergyClassUtils.primitiveToWrapper(returnType);
			}
			if (EnergyClassUtils.isTypeVoid(returnType) || Integer.class.equals(returnType)) {
				return;
			}

			throw new DaoGenerateException("方法[" + method + "]配置错误：配置了@Update注解的方法只能返回void,int或者Integer；或者请增加@ReturnId注解");

		}

	}
	
	@Override
	protected void logBindInfo(Method method) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("绑定" + getDescription() + "到方法[" + method + "]成功");
		}
	}
	
	private String getDescription() {
		String desc = "@Update(" + this.getParsedSql().getOriginalExpression() + ")";

		if(this.isReturnId()) {
			desc = desc + ",@ReturnId()";
		}
		
		return desc;
	}
}
