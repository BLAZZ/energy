package net.energy.jdbc.definition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.energy.annotation.ReturnId;
import net.energy.annotation.jdbc.BatchUpdate;
import net.energy.exception.DaoGenerateException;
import net.energy.expression.ParsedExpression;
import net.energy.utils.BatchDefinition;
import net.energy.utils.TypeUtils;

import org.apache.commons.lang.ClassUtils;

/**
 * 通过对配置了@BatchUpdate的方法的解析，产生需要在执行JDBC操作时必要用到的参数。
 * 
 * @author wuqh
 * 
 */
public class JdbcBatchUpdateDefinition extends BaseJdbcDefinition implements BatchDefinition {
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
		init(method);
	}

	private void init(Method method) throws DaoGenerateException {
		// 检查返回值类型
		checkReturnType(method);

		// 解析Parameter上的Annotation
		Map<String, Integer> paramIndexes = new HashMap<String, Integer>(8, 1f);
		Map<String, Integer> batchParamIndexes = new HashMap<String, Integer>(8, 1f);
		Class<?>[] paramTypes = method.getParameterTypes();
		Annotation[][] annotations = method.getParameterAnnotations();
		parseParameterAnnotations(annotations, paramIndexes, batchParamIndexes, paramTypes);

		// 解析批量修改的SQL语句
		BatchUpdate update = method.getAnnotation(BatchUpdate.class);
		parseSql(update.value(), paramTypes, paramIndexes, batchParamIndexes);

		// parseShardBy(method, paramIndexes, paramTypes);
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
			if (TypeUtils.isTypeList(returnType)) {
				isReturnList = true;
			} else if (!TypeUtils.isTypeArray(returnType)) {
				throw new DaoGenerateException("@ReturnId must on a method return List<? extends Number> or an array");
			} else {
				returnComponentType = returnType.getComponentType();
				if (!ClassUtils.isAssignable(returnComponentType, Number.class, true)) {
					throw new DaoGenerateException(
							"@ReturnId return an array that only can be primitive or assignable to Number");
				}

				isReturnList = false;
			}
		} else {
			if (void.class.equals(returnType) || int.class.equals(returnType.getComponentType())) {
				return;
			}
			throw new DaoGenerateException("@BatchUpdate must on a method return void or int[]");
		}
	}

	@Override
	public ParsedExpression getParsedExpression() {
		return getParsedSql();
	}
}
