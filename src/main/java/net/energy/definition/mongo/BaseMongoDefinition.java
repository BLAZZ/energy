package net.energy.definition.mongo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import net.energy.annotation.mongo.MongoCollection;
import net.energy.definition.AbstractDefintion;
import net.energy.exception.DaoGenerateException;
import net.energy.expression.ExpressionParser;
import net.energy.expression.ParsedExpression;
import net.energy.expression.ParserFacotory;
import net.energy.expression.ParserFacotory.ExpressionType;
import net.energy.utils.ClassHelper;
import net.energy.utils.ExpressionUtils;

import org.apache.commons.lang.StringUtils;

/**
 * 通过对配置了@MongoFind等方法的解析， 产生需要在执行Mongo操作时必要用到的参数。
 * BaseMongoDefinition中配置了一些共有的或者较为基本的参数。
 * 
 * @author wuqh
 * 
 */
public abstract class BaseMongoDefinition extends AbstractDefintion {
	/**
	 * 需要绑定数据的Shell
	 */
	protected String shellWithToken;

	/**
	 * <code>@MongoCollection</code>参数放在args的位置
	 * 
	 */
	protected int collectionIndex = -1;

	/**
	 * <code>@MongoCollection</code>中value的值
	 */
	protected String globalCollectionName;

	public BaseMongoDefinition(Method method) throws DaoGenerateException {
		super(method);
	}

	@Override
	protected void parseInternal(Method method, Map<String, Integer> paramIndexes,
			Map<String, Integer> batchParamIndexes) throws DaoGenerateException {
		MongoCollection collection = method.getAnnotation(MongoCollection.class);
		if (collection != null) {
			globalCollectionName = collection.value();
		}

		Annotation[][] annotations = method.getParameterAnnotations();
		Class<?>[] paramTypes = method.getParameterTypes();
		for (int index = 0; index < annotations.length; index++) {

			for (Annotation annotation : annotations[index]) {
				Class<? extends Annotation> annotationType = annotation.annotationType();
				if (MongoCollection.class.equals(annotationType)) {
					if (net.energy.utils.ClassHelper.isTypeString(paramTypes[index])) {
						collectionIndex = index;
					}
				}
			}
		}
	}

	@Override
	protected void checkBeforeParse(Method method) throws DaoGenerateException {
	}
	
	@Override
	protected void checkAfterParse(Method method) throws DaoGenerateException {
		if (collectionIndex == -1 && StringUtils.isEmpty(globalCollectionName)) {
			throw new DaoGenerateException("方法[" + method + "]配置错误：没有配置@MongoCollection注解");
		}
	}

	@Override
	protected ParsedExpression parseExpression(Method method) {
		ExpressionParser parser = ParserFacotory.createExpressionParser(ExpressionType.MONGO_SHELL);

		ParsedExpression parsedShell = parser.parse(getSourceShell(method));
		shellWithToken = ExpressionUtils.getSql(parsedShell);

		return parsedShell;
	}

	/**
	 * 获取方法中配置的原始Shell
	 * 
	 * @param method
	 * @return
	 */
	protected abstract String getSourceShell(Method method);

	/**
	 * 检查加了修改更新类注解的方法的返回值类型:必须返回void或者boolean，如果不是将抛出DaoGenerateException
	 * 
	 * @param method
	 * @param expcetionToThrow
	 * @throws DaoGenerateException
	 */
	protected void checkUpsetReturnType(Method method, String annotationName) throws DaoGenerateException {
		Class<?> returnType = method.getReturnType();

		if (net.energy.utils.ClassHelper.isTypePrimitive(returnType)) {
			returnType = ClassHelper.primitiveToWrapper(returnType);
		}
		if (net.energy.utils.ClassHelper.isTypeVoid(returnType) || Boolean.class.equals(returnType)) {
			return;
		}

		throw new DaoGenerateException("方法[" + method + "]配置错误：返回值非void,boolean,java.lang.Boolean类型，不能@" + annotationName
				+ "注解");

	}

	/**
	 * 获取需要操作collectionName
	 * 
	 * @param args
	 * @return
	 */
	public String getCollectionName(Object[] args) {
		if (collectionIndex == -1) {
			return globalCollectionName;
		}

		Object arg = args[collectionIndex];
		if (arg == null) {
			return globalCollectionName;
		} else {
			return arg.toString();
		}

	}

	public String getShellWithToken() {
		return shellWithToken;
	}

	public ParsedExpression getParsedShell() {
		return getParsedExpression();
	}

}
