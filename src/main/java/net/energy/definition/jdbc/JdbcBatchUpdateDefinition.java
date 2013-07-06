package net.energy.definition.jdbc;

import java.lang.reflect.Method;

import net.energy.annotation.ReturnId;
import net.energy.annotation.jdbc.BatchUpdate;
import net.energy.definition.BatchDefinition;
import net.energy.exception.DaoGenerateException;
import net.energy.utils.ClassHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通过对配置了@BatchUpdate的方法的解析，产生需要在执行JDBC操作时必要用到的参数。
 * 
 * @author wuqh
 * 
 */
public class JdbcBatchUpdateDefinition extends BaseJdbcDefinition implements BatchDefinition {
	private static final Logger LOGGER = LoggerFactory.getLogger(JdbcBatchUpdateDefinition.class);
	private boolean isReturnId = false;
	private boolean isReturnList = false;
	private Class<?> returnComponentType;

	public boolean isReturnId() {
		return isReturnId;
	}

	public boolean isReturnList() {
		return isReturnList;
	}

	public Class<?> getReturnComponentType() {
		return returnComponentType;
	}

	public JdbcBatchUpdateDefinition(Method method) throws DaoGenerateException {
		super(method);
	}

	protected String getSourceSql(Method method) {
		// 解析批量修改的SQL语句
		BatchUpdate update = method.getAnnotation(BatchUpdate.class);
		return update.value();
	}

	@Override
	protected void checkBeforeParse(Method method) throws DaoGenerateException {
		super.checkBeforeParse(method);
		checkReturnType(method);
	}

	/**
	 * 检查加了@BatchUpdate注解的方法的返回值类型。规则： 如果有@ReturnId就必须是Number的父类或者基本类型的数组或者List
	 * 如果没有@ReturnId就必须返回void或者int[]
	 * 
	 * @param method
	 * @throws DaoGenerateException
	 */
	private void checkReturnType(Method method) throws DaoGenerateException {
		isReturnId = (method.getAnnotation(ReturnId.class) != null);

		Class<?> returnType = method.getReturnType();
		if (isReturnId && returnType != null) {
			if (ClassHelper.isTypeList(returnType)) {
				isReturnList = true;
			} else if (!ClassHelper.isTypeArray(returnType)) {
				throw new DaoGenerateException("方法[" + method
						+ "]配置错误：配置了@ReturnId注解的方法只能返回数组或者List<? extends Number>类型对象");
			} else {
				returnComponentType = returnType.getComponentType();
				if (!ClassHelper.isAssignable(returnComponentType, Number.class, true)) {
					throw new DaoGenerateException("方法[" + method + "]配置错误：返回类型只能是基本类型数组或者java.lang.Number类型数组");
				}

				isReturnList = false;
			}
		} else {
			if (returnType == null || void.class.equals(returnType) || int.class.equals(returnType.getComponentType())) {
				return;
			}
			throw new DaoGenerateException("方法[" + method + "]配置错误：配置了@BatchUpdate注解的方法只能返回int[]；或者请增加@ReturnId注解");
		}
	}

	@Override
	protected void logBindInfo(Method method) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("绑定" + getDescription() + "到方法[" + method + "]成功");
		}
	}

	private String getDescription() {
		String desc = "@BatchUpdate(" + this.getParsedSql().getOriginalExpression() + ")";

		if (this.isReturnId()) {
			desc = desc + ",@ReturnId()";
		}

		return desc;
	}

}
