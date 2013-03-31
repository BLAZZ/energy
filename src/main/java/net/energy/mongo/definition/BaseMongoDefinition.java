package net.energy.mongo.definition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import net.energy.annotation.BatchParam;
import net.energy.annotation.Param;
import net.energy.annotation.mongo.MongoCollection;
import net.energy.exception.DaoGenerateException;
import net.energy.expression.ExpressionParser;
import net.energy.expression.ParsedExpression;
import net.energy.expression.ParserFacotory;
import net.energy.expression.ParserFacotory.ExpressionType;
import net.energy.utils.CommonUtils;
import net.energy.utils.TypeUtils;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 通过对配置了@MongoFind等方法的解析， 产生需要在执行Mongo操作时必要用到的参数。
 * BaseMongoDefinition中配置了一些共有的或者较为基本的参数。
 * 
 * @author wuqh
 * 
 */
public class BaseMongoDefinition {
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
	 * parsedShell的parameterNames中每个name对应对象在方法args[]数组中的索引值。
	 * 由于parameterNames包含"."，所以需要parameterIndexes记录"."之前的@Param、@BatchParam对应的位置
	 * 
	 * <pre>
	 * 例如：
	 * <code>@MongoBatchInsert("{id::user.id,nickname::user.nickname,date::user.date}")
	 * public void insertUsers(@BatchParam("user") User[] user, @MongoCollection() String collection);</code>
	 * 将解析出parsedSql.parameterNames=["user.id","user.nickname","user.date"]，但对应的parameterIndexes就会是[0,0,0]
	 * 
	 * 注意：@BatchParam对应的index也会当做@Param处理。
	 * 因为，args[]=[[{id:1L,nickname:'123',date:...},{id:2L,nickname:'1234',date:...}]]这样的参数在调用时会转换为[[{id:1L,nickname:'123',date:...}],[{id:2L,nickname:'1234',date:...}]]给Shell操作调用。
	 * 所以，对于@BatchParam中的每一个值，在实际调用过程中都相当于每次都是一个@Param
	 * </pre>
	 * 
	 */
	protected Integer[] parameterIndexes;
	/**
	 * 由于批量执行过程中，需要逐一替换参数中对映位置的值，所以需要记录每一个@BatchParam在args中的index
	 * 
	 * <pre>
	 * 例如：
	 * <code>@MongoBatchInsert("{id::user.id,nickname::user.nickname,date::user.date}")
	 * public void insertUsers(@BatchParam("user") User[] user, @MongoCollection() String collection);</code>
	 * 对应的batchParamIndexes就会是[0]
	 * </pre>
	 * 
	 */
	private Integer[] batchParamIndexes;

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
	protected void parseParameterAnnotations(Annotation[][] annotations, Map<String, Integer> paramIndexes,
			Map<String, Integer> batchParamIndexMap, Class<?>[] paramTypes) throws DaoGenerateException {
		for (int index = 0; index < annotations.length; index++) {

			for (Annotation annotation : annotations[index]) {
				Class<? extends Annotation> annotationType = annotation.annotationType();
				if (Param.class.equals(annotationType)) {
					Param param = (Param) annotation;
					String value = param.value();
					paramIndexes.put(value, index);
				}
				if (BatchParam.class.equals(annotationType) && batchParamIndexMap != null) {
					BatchParam param = (BatchParam) annotation;
					String value = param.value();
					batchParamIndexMap.put(value, index);
					batchParamIndexes = (Integer[]) ArrayUtils.add(batchParamIndexes, new Integer(index));

					if (paramTypes[index] == null || !paramTypes[index].isArray()) {
						throw new DaoGenerateException("@BatchParam can only on an array");
					}

				}
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

	public Integer[] getParameterIndexes() {
		return parameterIndexes;
	}

	public ParsedExpression getParsedShell() {
		return parsedShell;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public Integer[] getBatchParamIndexes() {
		return batchParamIndexes;
	}
}
