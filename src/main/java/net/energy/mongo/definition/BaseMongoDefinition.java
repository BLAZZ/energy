package net.energy.mongo.definition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import net.energy.annotation.mongo.MongoCollection;
import net.energy.exception.DaoGenerateException;
import net.energy.expression.ExpressionParser;
import net.energy.expression.ParsedExpression;
import net.energy.expression.ParserFacotory;
import net.energy.expression.ParserFacotory.ExpressionType;
import net.energy.utils.CommonUtils;
import net.energy.utils.ParameterParseable;
import net.energy.utils.TypeUtils;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 通过对配置了@MongoFind等方法的解析， 产生需要在执行Mongo操作时必要用到的参数。
 * BaseMongoDefinition中配置了一些共有的或者较为基本的参数。
 * 
 * @author wuqh
 * 
 */
public class BaseMongoDefinition extends ParameterParseable {
	/**
	 * 解析后的Shell值对象
	 */
	protected ParsedExpression parsedShell;
	/**
	 * 需要绑定数据的Shell
	 */
	protected String shellWithToken;
	/**
	 * 用于从方法参数的Bean对象中获取需要变量值的getter方法。缓存起来用于减少反射的查询操作
	 */
	protected Method[] getterMethods;

	/**
	 * <code>@MongoCollection</code>参数放在args的位置
	 * 
	 */
	protected int collectionIndex = -1;
	/**
	 * 分页对象位置
	 */
	protected int pageIndex = -1;
	/**
	 * <code>@MongoCollection</code>中value的值
	 */
	protected String globalCollectionName;

	public BaseMongoDefinition(Method method) {
		MongoCollection collection = method.getAnnotation(MongoCollection.class);
		if (collection != null) {
			globalCollectionName = collection.value();
		}
	}

	/**
	 * 解析方法参数上配置的所有annotation
	 * parameter上的annotation为一个二维数组，一个维度为参数个数，第二个对单个参数上的所有annotation
	 * 即：annotations[0]，即为args[0]上的所有参数。
	 * 
	 * @param method
	 * 
	 * @param annotations
	 *            parameter上的annotation
	 * @param paramIndexes
	 *            每个@Param参数在parameter数组中的位置，如@Param("user")在parameter[0]上，那么
	 *            paramIndexes.get("user")=0
	 * @param batchParamIndexMap
	 *            每个@BatchParam参数在parameter数组中的位置，如@BatchParam("user")在parameter
	 *            [0]上，那么 batchParamIndexMap("user")=0
	 * @param paramTypes
	 *            parameter的每个参数的类型
	 * 
	 */
	@Override
	protected void parseParameterAnnotations(Method method, Annotation[][] annotations,
			Map<String, Integer> paramIndexes, Map<String, Integer> batchParamIndexMap, Class<?>[] paramTypes)
			throws DaoGenerateException {
		super.parseParameterAnnotations(method, annotations, paramIndexes, batchParamIndexMap, paramTypes);
		
		parseExtentionParameterAnnotations(annotations, paramTypes);
		
	}
	
	protected void parseExtentionParameterAnnotations(Annotation[][] annotations, Class<?>[] paramTypes) throws DaoGenerateException {
		for (int index = 0; index < annotations.length; index++) {

			for (Annotation annotation : annotations[index]) {
				Class<? extends Annotation> annotationType = annotation.annotationType();
				if (MongoCollection.class.equals(annotationType)) {
					if (TypeUtils.isTypeString(paramTypes[index])) {
						collectionIndex = index;
					}
				}
			}
		}

		if (collectionIndex == -1 && StringUtils.isEmpty(globalCollectionName)) {
			throw new DaoGenerateException("Miss String type @MongoCollection in the mongo shell parameters");
		}
	}

	/**
	 * 根据原始的Shell等生成解析后的parsedShell、获取参数必须的getter方法、parameterIndexes、
	 * 以及shellWithToken
	 * 
	 * @param oriShell
	 *            <code>@MongoFind</code>等中配置的SQL
	 * @param paramTypes
	 *            parameter的每个参数的类型
	 * @param paramIndexes
	 *            每个@Param参数在parameter数组中的位置，如@Param("user")在parameter[0]上，那么
	 *            paramIndexes.get("user")=0
	 * @param batchParamIndexes
	 *            每个@BatchParam参数在parameter数组中的位置，如@BatchParam("user")在parameter
	 *            [0]上，那么 batchParamIndexMap("user")=0
	 * @throws DaoGenerateException
	 */
	protected void parseShell(String oriShell, Class<?>[] paramTypes, Map<String, Integer> paramIndexes,
			Map<String, Integer> batchParamIndexes) throws DaoGenerateException {
		ExpressionParser parser = ParserFacotory.createExpressionParser(ExpressionType.MONGO_SHELL);
		parsedShell = parser.parse(oriShell);
		List<String> parameterNames = parsedShell.getParameterNames();
		Object[] gettersAndIndexes = null;
		if (batchParamIndexes == null) {
			gettersAndIndexes = CommonUtils.getGettersAndIndexes(parameterNames, paramIndexes, paramTypes);
		} else {
			gettersAndIndexes = CommonUtils.getGettersAndIndexes(parameterNames, paramIndexes, batchParamIndexes,
					paramTypes);
		}
		Method[] getterMethods = (Method[]) gettersAndIndexes[0];
		Integer[] parameterIndexes = (Integer[]) gettersAndIndexes[1];

		this.getterMethods = getterMethods;
		this.parameterIndexes = parameterIndexes;
		this.shellWithToken = CommonUtils.getShell(parsedShell);
	}

	/**
	 * 检查加了修改更新类注解的方法的返回值类型:必须返回void或者boolean，如果不是将抛出DaoGenerateException
	 * 
	 * @param method
	 * @param expcetionToThrow
	 * @throws DaoGenerateException
	 */
	protected void checkUpsetReturnType(Method method, String annotationName) throws DaoGenerateException {
		Class<?> returnType = method.getReturnType();

		if (TypeUtils.isTypePrimitive(returnType)) {
			returnType = ClassUtils.primitiveToWrapper(returnType);
		}
		if (TypeUtils.isTypeVoid(returnType) || Boolean.class.equals(returnType)) {
			return;
		}

		throw new DaoGenerateException("if use @" + annotationName
				+ " only be used on a method return void,boolean,Boolean");

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

	public Method[] getGetterMethods() {
		return getterMethods;
	}

	public ParsedExpression getParsedShell() {
		return parsedShell;
	}

	public int getPageIndex() {
		return pageIndex;
	}

}
