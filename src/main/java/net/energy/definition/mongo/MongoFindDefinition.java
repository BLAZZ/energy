package net.energy.definition.mongo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import net.energy.annotation.Unique;
import net.energy.annotation.mongo.MongoFind;
import net.energy.annotation.mongo.MongoLimit;
import net.energy.annotation.mongo.MongoMapper;
import net.energy.annotation.mongo.MongoSkip;
import net.energy.annotation.mongo.MongoSort;
import net.energy.exception.DaoGenerateException;
import net.energy.mongo.BeanMapper;
import net.energy.mongo.impl.AutoDetectBeanMapper;
import net.energy.utils.ClassHelper;
import net.sf.cglib.core.ReflectUtils;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

/**
 * 通过对配置了@MongoFind方法的解析， 产生需要在执行Mongo查询操作时必要用到的参数。
 * 
 * @author wuqh
 * 
 */
public class MongoFindDefinition extends BaseMongoDefinition {
	private static final Logger LOGGER = LoggerFactory.getLogger(MongoFindDefinition.class);
	private BeanMapper<?> beanMapper;
	private boolean isUnique;
	private Integer skip;
	private int skipIndex = -1;
	private Integer limit;
	private int limitIndex = -1;
	private DBObject sortObject;
	private int batchSize;

	public BeanMapper<?> getBeanMapper() {
		return beanMapper;
	}

	public boolean isUnique() {
		return isUnique;
	}

	public MongoFindDefinition(Method method) throws DaoGenerateException {
		super(method);
	}

	@Override
	protected String getSourceShell(Method method) {
		MongoFind findShell = method.getAnnotation(MongoFind.class);
		return findShell.value();
	}

	@Override
	protected void parseInternal(Method method, Map<String, Integer> paramIndexes,
			Map<String, Integer> batchParamIndexes) throws DaoGenerateException {
		super.parseInternal(method, paramIndexes, batchParamIndexes);

		Annotation[][] annotations = method.getParameterAnnotations();
		Class<?>[] paramTypes = method.getParameterTypes();
		for (int index = 0; index < annotations.length; index++) {

			for (Annotation annotation : annotations[index]) {
				Class<? extends Annotation> annotationType = annotation.annotationType();
				if (MongoLimit.class.equals(annotationType)) {
					if (!ClassHelper.isAssignable(paramTypes[index], Integer.class, true)) {
						throw new DaoGenerateException("方法[" + method
								+ "]配置错误：@MongoLimit注解只能配置在int或者java.lang.Integer类型的参数上");
					}
					limitIndex = index;
				}
				if (MongoSkip.class.equals(annotationType)) {
					if (!ClassHelper.isAssignable(paramTypes[index], Integer.class, true)) {
						throw new DaoGenerateException("方法[" + method
								+ "]配置错误：@MongoSkip注解只能配置在int或者java.lang.Integer类型的参数上");
					}
					skipIndex = index;
				}
			}
		}

		// 解析MongoFind相关的Annotation配置:@MongoLimit,@MongoSkip,@MongoSort
		configFindExtension(method);
		// 解析是否包含@Unique配置
		configUnique(method);
		// 解析@MongoMapper配置
		configRowMapper(method);

		MongoFind findShell = method.getAnnotation(MongoFind.class);
		batchSize = findShell.batchSize();
	}

	/**
	 * 解析MongoFind相关的Annotation配置:@MongoLimit,@MongoSkip,@MongoSort
	 * 
	 * @param method
	 * @throws DaoGenerateException
	 */
	private void configFindExtension(Method method) throws DaoGenerateException {
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
				throw new DaoGenerateException("方法[" + method + "]配置错误：@MongoSort注解配置的表达式 不符合BSON格式");
			}

		}
	}

	/**
	 * 解析@MongoMapper配置，此方法必须在<code>configUnique</code>后执行，应为需要判断是否返回一条数据
	 * 
	 * @param method
	 * @throws DaoGenerateException
	 */
	@SuppressWarnings("rawtypes")
	private void configRowMapper(Method method) {
		MongoMapper mapper = method.getAnnotation(MongoMapper.class);
		Class<? extends BeanMapper> mapperType = AutoDetectBeanMapper.class;
		if (mapper != null) {
			mapperType = mapper.value();
		}

		if (AutoDetectBeanMapper.class.equals(mapperType)) {
			Class<?> type = method.getReturnType();
			if (ClassHelper.isTypeList(type)) {
				type = ClassHelper.getReturnGenericType(method);
			}

			beanMapper = (BeanMapper<?>) ReflectUtils.newInstance(mapperType, new Class[] { Class.class },
					new Object[] { type });
		} else {

			beanMapper = (BeanMapper) ReflectUtils.newInstance(mapperType);
		}

	}

	@Override
	protected void checkBeforeParse(Method method) throws DaoGenerateException {
		super.checkBeforeParse(method);

		MongoMapper mapper = method.getAnnotation(MongoMapper.class);
		if (mapper == null
				&& (ClassHelper.isTypeList(method.getReturnType()) && ClassHelper.getReturnGenericType(method) == null)) {
			throw new DaoGenerateException("方法[" + method + "]配置错误：返回值必须存在指定泛型类，或者请使用@MongoMapper注解");
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void checkAfterParse(Method method) throws DaoGenerateException {
		super.checkAfterParse(method);

		MongoMapper mapper = method.getAnnotation(MongoMapper.class);
		Class<? extends BeanMapper> mapperType = AutoDetectBeanMapper.class;
		if (mapper != null) {
			mapperType = mapper.value();
		}

		Class<?> returnType = method.getReturnType();
		if (isUnique && !AutoDetectBeanMapper.class.equals(mapperType)) {
			Class<?> expectedType = ClassHelper.getInterfaceGenericType(mapperType);
			if (!ClassHelper.isAssignable(returnType, expectedType, true)) {
				throw new DaoGenerateException("方法[" + method + "]配置错误：方法返回类型[" + returnType.getName()
						+ "]和@MongoMapper注解中配置的类型[" + expectedType.getName() + "]不一致；或者请去掉@MongoMapper注解");
			}
		} else {
			if (!ClassHelper.isTypeList(returnType)) {
				throw new DaoGenerateException("方法[" + method + "]配置错误：方法返回[java.util.List]类型 ，而实际返回类型["
						+ returnType.getName() + "]；或者请增加@Unique注解");
			}
		}
	}

	/**
	 * 解析是否包含@Unique配置，或者返回类型是否为List
	 * 
	 * @param method
	 */
	private void configUnique(Method method) {
		Unique unique = method.getAnnotation(Unique.class);
		Class<?> returnType = method.getReturnType();
		if (unique != null || !ClassHelper.isTypeList(returnType)) {
			isUnique = true;
		} else {
			isUnique = false;
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

	@Override
	protected void logBindInfo(Method method) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("绑定" + getDescription() + "到方法[" + method + "]成功");
		}
	}

	private String getDescription() {
		String desc = "@MongoFind(value=[" + this.getParsedShell().getOriginalExpression() + "],batchSize=["
				+ batchSize + "]),@MongoMapper(" + beanMapper.getClass() + ")";

		if (skipIndex != -1) {
			desc = ",@MongoSkip(" + skipIndex + ")";
		}

		if (limitIndex != -1) {
			desc = ",@MongoLimit(" + limitIndex + ")";
		}

		if (sortObject != null) {
			desc = ",@MongoSort(" + sortObject.toString() + ")";
		}

		if (this.isUnique) {
			desc = desc + ",@Unique()";
		}

		if (!StringUtils.isEmpty(globalCollectionName)) {
			desc = ",@MongoCollection(" + globalCollectionName + ")";
		}

		return desc;
	}
}
