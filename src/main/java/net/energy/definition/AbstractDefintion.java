package net.energy.definition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.energy.annotation.BatchParam;
import net.energy.annotation.GenericTable;
import net.energy.annotation.Param;
import net.energy.exception.DaoGenerateException;
import net.energy.expression.ParsedExpression;
import net.energy.utils.ArrayHelper;
import net.energy.utils.ClassHelper;
import net.energy.utils.Page;
import net.energy.utils.ReflectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DAO方法定义基础类，用于存@Param，@BatchParam，@GenericTable等公共信息
 * 
 * @author wuqh
 * 
 */
public abstract class AbstractDefintion {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDefintion.class);

	/**
	 * 系统保留关键字，用于指代方法执行的结果（用于单个对象缓存时生成versionKey）
	 */
	protected static String RESULT_PARAM_VALUE = "result";

	/**
	 * parsedSql的parameterNames中每个name对应对象在方法args[]数组中的索引值。
	 * 由于parameterNames包含"."，所以需要parameterIndexes记录"."之前的@Param、@BatchParam对应的位置
	 * 
	 * <pre>
	 * 例如：
	 * <code>@BatchUpdate("insert into photo(ownerId, albumId, file) values (:user.id, :album.id, :photo.file)")</code>
	 * <code>@ReturnId
	 * public List<Long> insertPhotosReturnIds(@Param("user") User user, @Param("album") Album album, @BatchParam("photo") Photo[] photo);</code>
	 * 将解析出parsedSql.parameterNames=["user.id","album.id","photo.file"]，但对应的parameterIndexes就会是[0,1,2]
	 * 
	 * 注意：@BatchParam对应的index也会当做@Param处理。
	 * 因为，args[]=[a,b,[c,d]]这样的参数在调用时会转换为[[a,b,c],[a,b,d]]给BatchSQL调用。
	 * 所以，对于@BatchParam中的每一个值，在实际调用过程中都相当于每次都是一个@Param
	 * </pre>
	 * 
	 */
	protected Integer[] parameterIndexes;

	/**
	 * 通用表名在参数的位置信息。
	 * 
	 * <pre>
	 * 例如：
	 * <code>@Query("select * from {0} where id=:id")</code>
	 * <code>public List queryCommentByResource(@Param("id") Object resourceId, @GenericTable(index=0) String commentType);</code>
	 * 将解析出的genericIndexes=[1]。
	 * 同理，如果存在一个{N}，那么genericIndexes[N]=（存在@GenericTable(index=N)annotation的参数在args中的index值）
	 * </pre>
	 * 
	 */
	protected Integer[] genericIndexes;

	/**
	 * 由于批量执行过程中，需要逐一替换参数中对映位置的值，所以需要记录每一个@BatchParam在args中的index
	 * 
	 * <pre>
	 * 例如：
	 * <code>@BatchUpdate("insert into photo(ownerId, albumId, file) values (:user.id, :album.id, :photo.file)")</code>
	 * <code>@ReturnId
	 * public List<Long> insertPhotosReturnIds(@Param("user") User user, @Param("album") Album album, @BatchParam("photo") Photo[] photo);</code>
	 * 对应的batchParamIndexes就会是{3}，即batchParamIndexes[0]=3
	 * </pre>
	 * 
	 */
	protected Integer[] batchParamIndexes;

	/**
	 * 分页对象所处参数序号
	 * 
	 */
	protected int pageIndex = -1;

	/**
	 * 用于执行的解析后的表达式
	 */
	protected ParsedExpression parsedExpression;

	/**
	 * 用于获取数据的getter方法
	 * 
	 */
	protected Method[] getterMethods;
	
	
	public AbstractDefintion(Method method) throws DaoGenerateException {
		//如果不传入method，则表示子类会自己根据需要，延迟初始化
		if(method != null) {
			initDefinition(method);
		}
	}
	
	/**
	 * 初始化DAO方法定义
	 * 
	 * @param method
	 * @throws DaoGenerateException
	 */
	protected void initDefinition(Method method) throws DaoGenerateException {
		checkBeforeParse(method);

		// 解析方法参数
		Class<?>[] paramTypes = method.getParameterTypes();
		Annotation[][] annotations = method.getParameterAnnotations();
		Map<String, Integer> paramIndexes = new HashMap<String, Integer>(8, 1f);
		Map<String, Integer> batchParamIndexes = new HashMap<String, Integer>(8, 1f);
		parseParameterAnnotations(method, annotations, paramIndexes, batchParamIndexes, paramTypes);

		// 根据参数序号信息，解析表达式，并生成getter方法和参数
		parsedExpression = parseExpression(method);
		List<String> parameterNames = parsedExpression.getParameterNames();
		Object[] gettersAndIndexes = getGettersAndIndexes(method, parameterNames, paramIndexes, batchParamIndexes);
		getterMethods = (Method[]) gettersAndIndexes[0];
		parameterIndexes = (Integer[]) gettersAndIndexes[1];

		parseInternal(method, paramIndexes, batchParamIndexes);

		checkAfterParse(method);
		
		logBindInfo(method);
	}

	/**
	 * 解析方法参数上配置的所有annotation，以及一些特殊的参数类型，例如{@link Page}
	 * parameter上的annotation为一个二维数组，一个维度为参数个数，第二个对单个参数上的所有annotation
	 * 即：annotations[0]，即为args[0]上的所有参数。
	 * 
	 * @param method
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
	private void parseParameterAnnotations(Method method, Annotation[][] annotations,
			Map<String, Integer> paramIndexes, Map<String, Integer> batchParamIndexMap, Class<?>[] paramTypes)
			throws DaoGenerateException {
		
		String[] paramNames = ReflectionUtils.PARAMETER_NAME_DISCOVERER.getParameterNames(method);
		
		for (int index = 0; index < annotations.length; index++) {
			boolean initParamName = true;
			boolean initBatchParamName = true;

			for (Annotation annotation : annotations[index]) {
				
				Class<? extends Annotation> annotationType = annotation.annotationType();
				if (Param.class.equals(annotationType)) {
					Param param = (Param) annotation;
					String value = param.value();

					if (RESULT_PARAM_VALUE.equals(value)) {
						throw new DaoGenerateException("方法[" + method + "]配置错误：不能使用@Param(\"" + RESULT_PARAM_VALUE + "\")注解作为参数：\""
								+ RESULT_PARAM_VALUE + "\"为系统保留关键字");
					}

					addParam(value, index, paramIndexes);
					initParamName = false;
				}
				if (BatchParam.class.equals(annotationType) && batchParamIndexMap != null) {
					BatchParam param = (BatchParam) annotation;
					String value = param.value();

					if (!ClassHelper.isTypeArray(paramTypes[index]) && !ClassHelper.isTypeList(paramTypes[index])) {
						throw new DaoGenerateException("方法[" + method + "]配置错误：@BatchParam只能配置在数组或者List实现类上");
					}

					addBatchParam(value, index, batchParamIndexMap);
					initBatchParamName = false;

				}
				if (GenericTable.class.equals(annotationType)) {
					GenericTable genericTable = (GenericTable) annotation;
					int order = genericTable.index();

					addGenericTable(index, order);
				}
			}
			
			if (paramNames == null || !initParamName || !initBatchParamName) {
				continue;
			}
			
			String paramName = paramNames[index];
			if (RESULT_PARAM_VALUE.equals(paramName)) {
				throw new DaoGenerateException("方法[" + method + "]配置错误：方法的参数名称不能使用保留关键字\"" + RESULT_PARAM_VALUE + "\"");
			}
			
			Class<?> type = paramTypes[index];

			if (ClassHelper.isTypeArray(type) || ClassHelper.isTypeList(type)) {
				if(initBatchParamName) {
					addBatchParam(paramName, index, batchParamIndexMap);
				}
			} else if(initParamName) {
				addParam(paramName, index, paramIndexes);
			}

			if (ClassHelper.isTypePage(type) && pageIndex == -1) {
				pageIndex = index;
			} else if (pageIndex != -1) {
				LOGGER.info("方法中已经存在Page类型对象，系统自动忽略");
			}
		}
		
	}

	/**
	 * 保存@BatchParam，或等价数组参数的参数序号到batchParamIndexMap
	 * 
	 * @param paramName
	 * @param index
	 * @param batchParamIndexMap
	 */
	protected void addBatchParam(String paramName, Integer index, Map<String, Integer> batchParamIndexMap) {
		if (batchParamIndexMap == null) {
			return;
		}
		Integer annotationIndex = batchParamIndexMap.get(paramName);
		if (annotationIndex != null) {
			LOGGER.info("已经存在@BatchParam(\"" + paramName + "\")注解");
			return;
		}
		batchParamIndexMap.put(paramName, index);
		batchParamIndexes = (Integer[]) ArrayHelper.add(batchParamIndexes, new Integer(index));
	}

	/**
	 * 保存@Param，或等价参数到paramIndexes
	 * 
	 * @param paramName
	 * @param index
	 * @param paramIndexes
	 */
	protected void addParam(String paramName, Integer index, Map<String, Integer> paramIndexes) {
		if (paramIndexes == null) {
			return;
		}
		Integer annotationIndex = paramIndexes.get(paramName);
		if (annotationIndex != null) {
			LOGGER.info("已经存在@Param注(\"" + paramName + "\")解");
			return;
		}
		paramIndexes.put(paramName, index);
	}

	/**
	 * 按照指定顺序保存@GenericTable的参数序号
	 * 
	 * @param index
	 * @param order
	 */
	protected void addGenericTable(Integer index, int order) {
		genericIndexes = net.energy.utils.ArrayHelper.addElemToArray(genericIndexes, index, order);
	}

	/**
	 * 根据解析后的值生成getter方法和每个getter方法对应的对象在参数列表中的序号
	 * 
	 * @param method
	 * @param parameterNames
	 * @param paramIndexes
	 * @param batchParamIndexes
	 * @return
	 * @throws DaoGenerateException
	 */
	protected Object[] getGettersAndIndexes(Method method, List<String> parameterNames,
			Map<String, Integer> paramIndexes, Map<String, Integer> batchParamIndexes) throws DaoGenerateException {
		Class<?>[] paramTypes = method.getParameterTypes();
		if (batchParamIndexes == null || batchParamIndexes.isEmpty()) {
			return ReflectionUtils.getGettersAndIndexes(parameterNames, paramIndexes, paramTypes);
		} else {
			return ReflectionUtils.getGettersAndIndexes(method, parameterNames, paramIndexes, batchParamIndexes, paramTypes);
		}
	}

	/**
	 * 解析方法上的表达式
	 * 
	 * @param method
	 * @return
	 */
	protected abstract ParsedExpression parseExpression(Method method);

	/**
	 * 用于在解析前检测配置的合法性
	 * 
	 * @param method
	 * @throws DaoGenerateException
	 */
	protected abstract void checkBeforeParse(Method method) throws DaoGenerateException;

	/**
	 * 用于解析实现类所必须的信息
	 * 
	 * @param method
	 * @param paramIndexes
	 * @param batchParamIndexes
	 * @throws DaoGenerateException
	 */
	protected abstract void parseInternal(Method method, Map<String, Integer> paramIndexes,
			Map<String, Integer> batchParamIndexes) throws DaoGenerateException;

	/**
	 * 用于在解析后检测配置的合法性
	 * 
	 * @param method
	 * @throws DaoGenerateException
	 */
	protected abstract void checkAfterParse(Method method) throws DaoGenerateException;
	
	/**
	 * 记录解析后的Annotation和方法的绑定信息
	 * 
	 * @param method
	 */
	protected abstract void logBindInfo(Method method);

	public Integer[] getParameterIndexes() {
		return parameterIndexes;
	}

	public Integer[] getGenericIndexes() {
		return genericIndexes;
	}

	public Integer[] getBatchParamIndexes() {
		return batchParamIndexes;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public ParsedExpression getParsedExpression() {
		return parsedExpression;
	}

	public Method[] getGetterMethods() {
		return getterMethods;
	}

	/**
	 * 获取参数中的分页对象
	 * 
	 * @param args
	 * @param pageIndex
	 * @return
	 */
	public Page getPageArgument(Object[] args) {
		if (pageIndex != -1) {
			return (Page) args[pageIndex];
		}
		return null;
	}
}
