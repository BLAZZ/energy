package net.energy.jdbc.definition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.energy.annotation.ReturnId;
import net.energy.annotation.jdbc.Update;
import net.energy.exception.DaoGenerateException;
import net.energy.utils.TypeUtils;

import org.apache.commons.lang.ClassUtils;

/**
 * 通过对配置了@Update的方法的解析，产生需要在执行JDBC操作时必要用到的参数。
 * 
 * @author wuqh
 * 
 */
public class JdbcUpdateDefinition extends BaseJdbcDefinition {
	private boolean isReturnId = false;

	public boolean isReturnId() {
		return isReturnId;
	}

	public JdbcUpdateDefinition(Method method) throws DaoGenerateException {
		init(method);
	}

	private void init(Method method) throws DaoGenerateException {
		// 检查返回值类型
		checkReturnType(method);

		// 解析Parameter上的Annotation
		Map<String, Integer> paramIndexes = new HashMap<String, Integer>(8, 1f);
		Annotation[][] annotations = method.getParameterAnnotations();
		Class<?>[] paramTypes = method.getParameterTypes();
		parseParameterAnnotations(method, annotations, paramIndexes, null, paramTypes);

		// 解析SQL语句
		Update update = method.getAnnotation(Update.class);
		String sql = update.value();
		parseSql(sql, paramTypes, paramIndexes);

		// parseShardBy(method, paramIndexes, paramTypes);
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
			if (TypeUtils.isTypePrimitive(returnType)) {
				returnType = ClassUtils.primitiveToWrapper(returnType);
			}
			if (!TypeUtils.isTypeNumber(returnType)) {
				throw new DaoGenerateException("if use @ReturnId: " + returnType + " must instance of Number");
			}
		} else {
			if (TypeUtils.isTypePrimitive(returnType)) {
				returnType = ClassUtils.primitiveToWrapper(returnType);
			}
			if (TypeUtils.isTypeVoid(returnType) || Integer.class.equals(returnType)) {
				return;
			}

			throw new DaoGenerateException("if use @Update only be used on a method return void,int,Integer");

		}

	}
}
