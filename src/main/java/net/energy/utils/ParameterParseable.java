package net.energy.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import net.energy.annotation.BatchParam;
import net.energy.annotation.GenericTable;
import net.energy.annotation.Param;
import net.energy.exception.DaoGenerateException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class ParameterParseable {
	private static final Log LOGGER = LogFactory.getLog(ParameterParseable.class);
	
	/**
	 * 单个对象缓存versionKey需要用于指代结果对象的保留字
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
	protected void parseParameterAnnotations(Method method, Annotation[][] annotations,
			Map<String, Integer> paramIndexes, Map<String, Integer> batchParamIndexMap, Class<?>[] paramTypes)
			throws DaoGenerateException {
		for (int index = 0; index < annotations.length; index++) {

			for (Annotation annotation : annotations[index]) {
				Class<? extends Annotation> annotationType = annotation.annotationType();
				if (Param.class.equals(annotationType)) {
					Param param = (Param) annotation;
					String value = param.value();

					if (RESULT_PARAM_VALUE.equals(value)) {
						throw new DaoGenerateException("@Param(\"" + RESULT_PARAM_VALUE
								+ "\") is illegal, it is a  because\"" + RESULT_PARAM_VALUE + "\" is a reserved word");
					}

					addParam(value, index, paramIndexes);
				}
				if (BatchParam.class.equals(annotationType) && batchParamIndexMap != null) {
					BatchParam param = (BatchParam) annotation;
					String value = param.value();

					if (paramTypes[index] == null || !paramTypes[index].isArray()) {
						throw new DaoGenerateException("@BatchParam can only on an array");
					}

					addBatchParam(value, index, batchParamIndexMap);

				}
				if (GenericTable.class.equals(annotationType)) {
					GenericTable genericTable = (GenericTable) annotation;
					int order = genericTable.index();

					addGenericTable(index, order);
				}
			}
		}

		parseParamterNames(method, paramIndexes, batchParamIndexMap, paramTypes);
	}

	protected void addBatchParam(String paramName, Integer index, Map<String, Integer> batchParamIndexMap) {
		if (batchParamIndexMap == null) {
			return;
		}
		Integer annotationIndex = batchParamIndexMap.get(paramName);
		if (annotationIndex != null) {
			LOGGER.info("batch parameter who's name is" + paramName +" has already exist");
			return;
		}
		batchParamIndexMap.put(paramName, index);
		batchParamIndexes = (Integer[]) ArrayUtils.add(batchParamIndexes, new Integer(index));
	}

	protected void addParam(String paramName, Integer index, Map<String, Integer> paramIndexes) {
		if (paramIndexes == null) {
			return;
		}
		Integer annotationIndex = paramIndexes.get(paramName);
		if (annotationIndex != null) {
			LOGGER.info("parameter who's name is" + paramName +" has already exist");
			return;
		}
		paramIndexes.put(paramName, index);
	}

	protected void addGenericTable(Integer index, int order) {
		genericIndexes = CommonUtils.addElemToArray(genericIndexes, index, order);
	}

	protected void parseParamterNames(Method method, Map<String, Integer> paramIndexes,
			Map<String, Integer> batchParamIndexMap, Class<?>[] paramTypes) throws DaoGenerateException {
		String[] paramNames = CommonUtils.PARAMETER_NAME_DISCOVERER.getParameterNames(method);
		if (paramNames == null) {
			return;
		}

		for (int i = 0; i < paramNames.length; i++) {
			String paramName = paramNames[i];

			if (RESULT_PARAM_VALUE.equals(paramName)) {
				throw new DaoGenerateException("parameter can not name result, because \"" + RESULT_PARAM_VALUE
						+ "\" is a reserved word");
			}

			Class<?> type = paramTypes[i];

			if (type == null || !type.isArray()) {
				addParam(paramName, i, paramIndexes);
			} else {
				addBatchParam(paramName, i, batchParamIndexMap);
			}
		}

	}

	public Integer[] getParameterIndexes() {
		return parameterIndexes;
	}

	public Integer[] getGenericIndexes() {
		return genericIndexes;
	}

	public Integer[] getBatchParamIndexes() {
		return batchParamIndexes;
	}
}
