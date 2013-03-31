package net.energy.mongo.definition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.energy.annotation.mongo.MongoCount;
import net.energy.exception.DaoGenerateException;
import net.energy.utils.TypeUtils;

import org.apache.commons.lang.ClassUtils;

/**
 * 通过对配置了@MongoCount方法的解析， 产生需要在执行Mongo统计操作时必要用到的参数。
 * 
 * @author wuqh
 * 
 */
public class MongoCountDefinition extends BaseMongoDefinition {
	public MongoCountDefinition(Method method) throws DaoGenerateException {
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

		MongoCount countShell = method.getAnnotation(MongoCount.class);
		String shell = countShell.value();

		// 解析Shell语句
		parseShell(shell, paramTypes, paramIndexes, null);
	}

	/**
	 * 检测返回值，返回值必须是int或其装箱类型
	 * 
	 * @param method
	 * @throws DaoGenerateException
	 */
	private void checkReturnType(Method method) throws DaoGenerateException {
		Class<?> returnType = method.getReturnType();

		if (TypeUtils.isTypePrimitive(returnType)) {
			returnType = ClassUtils.primitiveToWrapper(returnType);
		}
		if (Integer.class.equals(returnType)) {
			return;
		}

		throw new DaoGenerateException("if use @MongoCount only be used on a method return int or Integer");

	}

}
