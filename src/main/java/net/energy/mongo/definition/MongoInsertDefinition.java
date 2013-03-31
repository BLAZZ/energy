package net.energy.mongo.definition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.energy.annotation.mongo.MongoInsert;
import net.energy.exception.DaoGenerateException;

/**
 * 通过对配置了@MongoInsert方法的解析， 产生需要在执行Mongo插入操作时必要用到的参数。
 * 
 * @author wuqh
 * 
 */
public class MongoInsertDefinition extends BaseMongoDefinition {
	public MongoInsertDefinition(Method method) throws DaoGenerateException {
		super(method);
		init(method);
	}

	private void init(Method method) throws DaoGenerateException {
		// 检查返回值类型
		checkReturnType(method);

		// 解析Parameter上的Annotation
		Map<String, Integer> paramIndexes = new HashMap<String, Integer>(8, 1f);

		Class<?>[] paramTypes = method.getParameterTypes();
		Annotation[][] annotations = method.getParameterAnnotations();

		parseParameterAnnotations(annotations, paramIndexes, null, paramTypes);

		MongoInsert insertShell = method.getAnnotation(MongoInsert.class);
		String shell = insertShell.value();

		// 解析Shell语句
		parseShell(shell, paramTypes, paramIndexes, null);
	}

	/**
	 * 检查加了@MongoInsert注解的方法的返回值类型:必须返回void或者boolean
	 * 
	 * @param method
	 * @throws DaoGenerateException
	 */
	private void checkReturnType(Method method) throws DaoGenerateException {
		checkUpsetReturnType(method, "MongoInsert");
	}

}
