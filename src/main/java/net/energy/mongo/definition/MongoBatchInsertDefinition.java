package net.energy.mongo.definition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.energy.annotation.mongo.MongoBatchInsert;
import net.energy.exception.DaoGenerateException;
import net.energy.expression.ParsedExpression;
import net.energy.utils.BatchDefinition;

/**
 * 通过对配置了@MongoBatchInsert方法的解析， 产生需要在执行Mongo批量插入操作时必要用到的参数。
 * 
 * @author wuqh
 * 
 */
public class MongoBatchInsertDefinition extends BaseMongoDefinition implements BatchDefinition {
	public MongoBatchInsertDefinition(Method method) throws DaoGenerateException {
		super(method);
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

		MongoBatchInsert insertShell = method.getAnnotation(MongoBatchInsert.class);
		String shell = insertShell.value();
		// 解析批量插入的Shell语句
		parseShell(shell, paramTypes, paramIndexes, batchParamIndexes);
	}

	/**
	 * 检查加了@MongoBatchInsert注解的方法的返回值类型:必须返回void或者boolean
	 * 
	 * @param method
	 * @throws DaoGenerateException
	 */
	private void checkReturnType(Method method) throws DaoGenerateException {
		checkUpsetReturnType(method, "MongoBatchInsert");
	}

	@Override
	public ParsedExpression getParsedExpression() {
		return getParsedShell();
	}

}
