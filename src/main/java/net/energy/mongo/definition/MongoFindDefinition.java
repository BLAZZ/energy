package net.energy.mongo.definition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.energy.annotation.Param;
import net.energy.annotation.Unique;
import net.energy.annotation.mongo.MongoCollection;
import net.energy.annotation.mongo.MongoFind;
import net.energy.annotation.mongo.MongoLimit;
import net.energy.annotation.mongo.MongoMapper;
import net.energy.annotation.mongo.MongoSkip;
import net.energy.annotation.mongo.MongoSort;
import net.energy.exception.DaoGenerateException;
import net.energy.mongo.BeanMapper;
import net.energy.utils.GenericUtils;
import net.energy.utils.TypeUtils;
import net.sf.cglib.core.ReflectUtils;

import org.apache.commons.lang.ClassUtils;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

/**
 * 通过对配置了@MongoFind方法的解析， 产生需要在执行Mongo查询操作时必要用到的参数。
 * 
 * @author wuqh
 * 
 */
public class MongoFindDefinition extends BaseMongoDefinition {
	private BeanMapper<?> beanMapper;
	private boolean isUnique;
	private Integer skip;
	private int skipIndex = -1;
	private Integer limit;
	private int limitIndex = -1;
	DBObject sortObject;
	private int batchSize;

	public BeanMapper<?> getBeanMapper() {
		return beanMapper;
	}

	public boolean isUnique() {
		return isUnique;
	}

	public MongoFindDefinition(Method method) throws DaoGenerateException {
		super(method);
		init(method);
	}

	private void init(Method method) throws DaoGenerateException {
		// 解析MongoFind相关的Annotation配置:@MongoLimit,@MongoSkip,@MongoSort
		configFindExtention(method);
		// 解析是否包含@Unique配置
		configUnique(method);
		// 解析@MongoMapper配置
		configRowMapper(method);

		// 解析Parameter上的Annotation
		Map<String, Integer> paramIndexes = new HashMap<String, Integer>(8, 1f);

		Class<?>[] paramTypes = method.getParameterTypes();
		Annotation[][] annotations = method.getParameterAnnotations();
		for (int index = 0; index < annotations.length; index++) {
			if (TypeUtils.isTypePage(paramTypes[index])) {
				pageIndex = index;
				break;
			}
		}

		parseParameterAnnotations(annotations, paramIndexes, paramTypes);

		MongoFind findShell = method.getAnnotation(MongoFind.class);
		String shell = findShell.value();
		batchSize = findShell.batchSize();

		// 解析Shell语句
		parseShell(shell, paramTypes, paramIndexes, null);
	}

	/**
	 * 解析MongoFind相关的Annotation配置:@MongoLimit,@MongoSkip,@MongoSort
	 * 
	 * @param method
	 * @throws DaoGenerateException
	 */
	private void configFindExtention(Method method) throws DaoGenerateException {
		MongoLimit mongoLimit = method.getAnnotation(MongoLimit.class);
		if (mongoLimit != null && mongoLimit.value() != -1) {
			limit = mongoLimit.value();
		}

		MongoSkip mongoSkip = method.getAnnotation(MongoSkip.class);
		if (mongoSkip != null && mongoSkip.value() != -1) {
			skip = mongoSkip.value();
		}

		MongoSort sort = method.getAnnotation(MongoSort.class);
		if (sort != null) {
			try {
				sortObject = (DBObject) JSON.parse(sort.value());
			} catch (Exception e) {
				throw new DaoGenerateException("Illegal Expression of @MongoSort value");
			}

		}
	}

	/**
	 * 解析@MongoMapper配置，此方法必须在<code>configUnique</code>后执行，应为需要判断是否返回一条数据
	 * 
	 * @param method
	 * @throws DaoGenerateException
	 */
	private void configRowMapper(Method method) throws DaoGenerateException {
		MongoMapper mapper = method.getAnnotation(MongoMapper.class);
		Class<? extends BeanMapper<?>> mapperType = null;
		if (mapper == null) {
			throw new DaoGenerateException("Missing @MongoMapper on a find @MongoShell");
		} else {
			mapperType = mapper.value();
		}
		beanMapper = (BeanMapper<?>) ReflectUtils.newInstance(mapperType);

		Class<?> returnType = method.getReturnType();
		if (isUnique) {
			Class<?> expectedType = GenericUtils.getGenericType(mapperType);
			if (!ClassUtils.isAssignable(returnType, expectedType, true)) {
				throw new DaoGenerateException("Return type is expected as '" + expectedType.getName()
						+ "' with @Unique, but actually is '" + returnType.getName() + "' in method: "
						+ method.toString() + ", missing @MongoMapper");
			}
		} else {
			if (!TypeUtils.isTypeList(returnType)) {
				throw new DaoGenerateException(
						"Return type is expected as 'java.util.List' without @Unique, but actually is '"
								+ returnType.getName() + "' in method: " + method.toString());
			}
		}

	}

	/**
	 * 解析是否包含@Unique配置
	 * 
	 * @param method
	 */
	private void configUnique(Method method) {
		Unique unique = method.getAnnotation(Unique.class);
		if (unique != null) {
			isUnique = true;
		} else {
			isUnique = false;
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
	 * @param paramTypes
	 *            parameter的每个参数的类型
	 * 
	 */
	protected void parseParameterAnnotations(Annotation[][] annotations, Map<String, Integer> paramIndexes,
			Class<?>[] paramTypes) throws DaoGenerateException {
		for (int index = 0; index < annotations.length; index++) {

			for (Annotation annotation : annotations[index]) {
				Class<? extends Annotation> annotationType = annotation.annotationType();
				if (Param.class.equals(annotationType)) {
					Param param = (Param) annotation;
					String value = param.value();
					paramIndexes.put(value, index);
				}
				if (MongoCollection.class.equals(annotationType)) {
					if (TypeUtils.isTypeString(paramTypes[index])) {
						collectionIndex = index;
					}
				}
				if (MongoLimit.class.equals(annotationType)) {
					limitIndex = index;
				}
				if (MongoSkip.class.equals(annotationType)) {
					skipIndex = index;
				}
			}
		}

		if (collectionIndex == -1 && globalCollectionName == null) {
			throw new DaoGenerateException("Miss String type @MongoCollection in the mongo shell parameters");
		}
	}

	public Integer getSkip(Object[] args) {
		if (skipIndex == -1) {
			return skip;
		}

		Object arg = args[skipIndex];
		if (arg == null) {
			return null;
		} else if (arg instanceof Integer) {
			return (Integer) arg;
		} else {
			return null;
		}
	}

	public Integer getLimit(Object[] args) {
		if (limitIndex == -1) {
			return limit;
		}

		Object arg = args[limitIndex];
		if (arg == null) {
			return null;
		} else if (arg instanceof Integer) {
			return (Integer) arg;
		} else {
			return null;
		}
	}

	public int getBatchSize() {
		return batchSize;
	}

	public DBObject getSortObject() {
		return sortObject;
	}
}
