package net.energy.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.energy.exception.DaoGenerateException;
import net.energy.expression.ParsedExpression;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 共用工具类
 * 
 * @author wuqh
 *
 */
public final class CommonUtils {
	private static final Log LOGGER = LogFactory.getLog(CommonUtils.class);
	public static final String SHELL_TOKEN = "#";
	private static final String SQL_TOKEN = "?";

	private CommonUtils() {
	}

	/**
	 * 将一个元素放到数组的指定位置上，如果数组不存在将创建数组。如果数组长度不够则会自动补全到index+1的长度。
	 * 
	 * @param <T>
	 * @param array
	 * @param element
	 * @param index
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] addElemToArray(T[] array, T element, int index) {
		if (array == null) {
			array = (T[]) Array.newInstance(element.getClass(), (index + 1));
			array[index] = element;
			return array;
		}
		if (index < array.length) {
			array[index] = element;
			return array;
		} else {
			T[] newArray = (T[]) Array.newInstance(element.getClass(), (index + 1));
			System.arraycopy(array, 0, newArray, 0, array.length);
			newArray[index] = element;
			return newArray;
		}

	}

	/**
	 * 获取参数中的分页对象
	 * 
	 * @param args
	 * @param pageIndex
	 * @return
	 */
	public static Page getPageArgument(Object[] args, int pageIndex) {
		if (pageIndex != -1) {
			return (Page) args[pageIndex];
		}
		return null;
	}

	/**
	 * 通过一组getter方法，提取args中指定位置对象中相应属性的值。
	 * 
	 * @param getterMethods
	 * @param parameterIndexes
	 * @param args
	 * @param parameterNames
	 * @return
	 */
	public static Object[] fetchVlaues(Method[] getterMethods, Integer[] parameterIndexes, Object[] args,
			List<String> parameterNames) {
		Object[] values = new Object[getterMethods.length];
		for (int i = 0; i < getterMethods.length; i++) {
			Method method = getterMethods[i];
			Integer index = parameterIndexes[i];

			Object value = fetchVlaue(method, index, args, parameterNames);
			values[i] = value;
		}
		return values;
	}

	/**
	 * 通过getter方法提取对象中相应属性的值，其实就对args中指定index的对象的getter方法的一次反射调用，或者map的一次get操作。
	 * 
	 * @param getterMethod
	 *            getter方法
	 * @param index
	 *            args中的index
	 * @param args
	 *            方法调用的实际参数
	 * @param parameterName
	 *            传入参数名称，这个在对象为Map时使用
	 * @return 提取到的值
	 */
	@SuppressWarnings("rawtypes")
	public static Object fetchVlaue(Method getterMethod, Integer index, Object[] args, String paramName) {
		if (index == null || index == -1) {
			return null;
		}

		Object arg = args[index];
		
		if(arg == null) {
			return null;
		}

		if (getterMethod == null) {
			Class<?> clazz = arg.getClass();
			// 如果是枚举类型需要转换为String，因为大多数据库都不识别这个类型。
			if (clazz.isEnum()) {
				return arg.toString();
			}
			// 如果Map，执行"."操作就是执行get操作
			if (TypeUtils.isTypeMap(clazz)) {
				Map map = (Map) arg;
				int pos = paramName.indexOf('.');
				String prop = paramName.substring(pos + 1);
				Object result = map.get(prop);
				if (result.getClass().isEnum()) {
					return result.toString();
				}
				return result;
			}

			return arg;
		} else {
			try {
				Object value = getterMethod.invoke(arg, new Object[0]);
				if (value.getClass().isEnum()) {
					return value.toString();
				} else {
					return value;
				}
			} catch (Throwable e) {
				LOGGER.info("invoke method failed", e);
				return null;
			}
		}
	}

	/**
	 * 通过getter方法提取对象中相应属性的值，其实就对args中指定index的对象的getter方法的一次反射调用，或者map的一次get操作。
	 * 
	 * @param getterMethod
	 *            getter方法
	 * @param index
	 *            args中的index
	 * @param args
	 *            方法调用的实际参数
	 * @param parameterNames
	 *            传入参数名称，这个在对象为Map时使用
	 * @return 提取到的值
	 */
	public static Object fetchVlaue(Method getterMethod, Integer index, Object[] args, List<String> parameterNames) {
		if (index == null || index == -1) {
			return null;
		}

		String paramName = parameterNames.get(index);

		return fetchVlaue(getterMethod, index, args, paramName);
	}

	/**
	 * 解析出bean的getter方法,和parameterNames中个参数对应的args中的索引值
	 * 由于parameterNames包含"."，所以需要indexes记录"."之前的@Param对应的位置
	 * 
	 * <pre>
	 * 例如：
	 * <code>@CacheDelete(key="album-d-:albumId-:ownerId")</code>
	 * <code>@VerUpdate(vkey="ALBUM-:owner.id-:album.id-v")</code>
	 * public void updatePhoto(@Param("owner") User owner, @Param("album") Album album);</code>
	 * 将传入：parameterNames=["album.id","owner.id"]，paramIndexes={"album"=1,"owner"=0},paramTypes=[User.class,Album.class]
	 * 生成：[[User.getId,Album.getId],[1,0]]
	 * </pre>
	 * 
	 * 
	 * @param parameterNames
	 * @param paramIndexes
	 * @param paramTypes
	 * @return
	 * @throws DaoGenerateException
	 */
	public static Object[] getGettersAndIndexes(List<String> parameterNames, Map<String, Integer> paramIndexes,
			Class<?>[] paramTypes) throws DaoGenerateException {
		int length = parameterNames.size();
		Method[] getters = new Method[length];
		Integer[] parameterIndexes = new Integer[length];
		for (int i = 0; i < length; i++) {
			String paramName = parameterNames.get(i);
			int pos = paramName.indexOf('.');
			if (pos != -1) {
				String actualName = paramName.substring(0, pos);

				int index = getParameIndex(paramIndexes, actualName);
				parameterIndexes[i] = index;

				String prop = paramName.substring(pos + 1);
				Method getter = findGetterByPropertyName(paramTypes[index], prop);
				getter.setAccessible(true); // no check to upgrade performance
				getters[i] = getter;
			} else { // don't need getters
				getters[i] = null;
				Integer index = getParameIndex(paramIndexes, paramName);
				parameterIndexes[i] = index;
			}
		}
		return new Object[] { getters, parameterIndexes };
	}

	/**
	 * 在paramIndexes获取查找key为paramName的值。如果不包含paramName，则会抛出DaoGenerateException
	 * 如传入：paramIndexes={"album"=1,"owner"=0},paramName="owner"，将返回0
	 * 
	 * @param paramIndexes
	 * @param paramName
	 * @return
	 * @throws DaoGenerateException
	 */
	public static Integer getParameIndex(Map<String, Integer> paramIndexes, String paramName)
			throws DaoGenerateException {
		if (!paramIndexes.containsKey(paramName)) {
			throw new DaoGenerateException("Missing @Param(\"" + paramName + "\") in parameter of method");
		}
		Integer index = paramIndexes.get(paramName);
		return index;
	}

	/**
	 * 查找Bean中指定属性的getter方法。如果找不到将抛出DaoGenerateException
	 * 
	 * @param clazz
	 *            Bean的类型
	 * @param prop
	 *            属性名称
	 * @return
	 * @throws DaoGenerateException
	 */
	public static Method findGetterByPropertyName(Class<?> clazz, String prop) throws DaoGenerateException {
		if (TypeUtils.isTypeMap(clazz)) {
			return null;
		}
		String name = Character.toUpperCase(prop.charAt(0)) + prop.substring(1);
		String getter = "get" + name;
		String is = "is" + name;
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			Class<?> returnType = method.getReturnType();
			if (method.getParameterTypes().length == 0 && !returnType.equals(void.class)) {
				if (getter.equals(method.getName())) {
					return method;
				}
				if (is.equals(method.getName())
						&& (returnType.equals(boolean.class) || returnType.equals(Boolean.class))) {
					return method;
				}
			}
		}
		throw new DaoGenerateException("Cannot find getter of property '" + prop + "' of class: " + clazz.getName());
	}

	/**
	 * 解析出bean的getter方法,和parameterNames中个参数对应的args中的索引值
	 * 由于parameterNames包含"."，所以需要indexes记录"."之前的@Param，@BatchParam对应的位置
	 * 
	 * <pre>
	 * 例如：
	 * <code>@BatchUpdate("insert into photo(ownerId, albumId, file) values (:user.id, :album.id, :photo.file)")</code>
	 * <code>@ReturnId
	 * public List<Long> insertPhotosReturnIds(@Param("user") User user, @Param("album") Album album, @BatchParam("photo") Photo[] photo);</code>
	 * 将传入：parameterNames=["owner.id","album.id","photo.file"]，paramIndexes={""owner"=0,album"=1},batchParamIndexes={"photo"=2},paramTypes=[User.class,Album.class,Photo.class]
	 * 生成：[[User.getId,Album.getId,Photo.getFile],[0,1,2]]
	 * </pre>
	 * 
	 * 
	 * @param parameterNames
	 * @param paramIndexes
	 * @param paramTypes
	 * @return
	 * @throws DaoGenerateException
	 */
	public static Object[] getGettersAndIndexes(List<String> parameterNames, Map<String, Integer> paramIndexes,
			Map<String, Integer> batchParamIndexes, Class<?>[] paramTypes) throws DaoGenerateException {
		int length = parameterNames.size();
		Method[] getters = new Method[length];
		Integer[] parameterIndexes = new Integer[length];
		for (int i = 0; i < length; i++) {
			String paramName = parameterNames.get(i);
			int pos = paramName.indexOf('.');
			if (pos != -1) {
				String actualName = paramName.substring(0, pos);

				Class<?> componentType = null;
				Integer index = null;

				if (!paramIndexes.containsKey(actualName) && !batchParamIndexes.containsKey(actualName)) {
					throw new DaoGenerateException("Missing @Param(\"" + actualName + "\") or @BatchParam(\""
							+ actualName + "\") in parameter of method");
				} else if (paramIndexes.containsKey(actualName)) {
					index = paramIndexes.get(actualName);
					componentType = paramTypes[index];
				} else {
					index = batchParamIndexes.get(actualName);

					Class<?> paramType = paramTypes[index];
					if (!paramType.isArray()) {
						throw new DaoGenerateException("@BatchParam(\"" + paramName
								+ "\") only can be used on an array in parameter of method");
					}
					componentType = paramTypes[index].getComponentType();
				}

				parameterIndexes[i] = index;
				String prop = paramName.substring(pos + 1);
				Method getter = findGetterByPropertyName(componentType, prop);
				getter.setAccessible(true); // no check to upgrade performance
				getters[i] = getter;

			} else { // don't need getters
				getters[i] = null;
				if (!paramIndexes.containsKey(paramName) && !batchParamIndexes.containsKey(paramName)) {
					throw new DaoGenerateException("Missing @Param(\"" + paramName + "\") or @BatchParam(\""
							+ paramName + "\")  in parameter of method");
				} else if (paramIndexes.containsKey(paramName)) {
					Integer index = paramIndexes.get(paramName);
					parameterIndexes[i] = index;
				} else {
					Integer index = batchParamIndexes.get(paramName);
					parameterIndexes[i] = index;
				}
			}
		}
		return new Object[] { getters, parameterIndexes };
	}

	/**
	 * 生成PreparedSQL
	 * 
	 * @param parsedExpression
	 * @return
	 */
	public static String getSql(ParsedExpression parsedExpression) {
		return getExpressionWithToken(parsedExpression, SQL_TOKEN);
	}

	/**
	 * 生成MongoShell
	 * 
	 * @param parsedExpression
	 * @return
	 */
	public static String getShell(ParsedExpression parsedExpression) {
		return getExpressionWithToken(parsedExpression, SHELL_TOKEN);
	}

	/**
	 * 生成表达式，将parsedExpression中的参数使用token来替换
	 * 
	 * @param parsedExpression
	 * @param token
	 * @return
	 */
	private static String getExpressionWithToken(ParsedExpression parsedExpression, String token) {
		String originalExpression = parsedExpression.getOriginalExpression();
		StringBuilder actualExpression = new StringBuilder();
		List<String> paramNames = parsedExpression.getParameterNames();
		int lastIndex = 0;
		for (int i = 0; i < paramNames.size(); i++) {
			int[] indexes = parsedExpression.getParameterIndexes(i);
			int startIndex = indexes[0];
			int endIndex = indexes[1];
			actualExpression.append(originalExpression.substring(lastIndex, startIndex));
			actualExpression.append(token);
			lastIndex = endIndex;
		}
		actualExpression.append(originalExpression.substring(lastIndex, originalExpression.length()));
		return actualExpression.toString();
	}

	/**
	 * 提取array[index]的值
	 * 
	 * @param array
	 * @param index
	 * @return
	 */
	public static Object fetchArrayValue(Object array, int index) {
		if(array == null) {
			return null;
		}
		
		Class<?> clazz = array.getClass();
		if (!clazz.isArray()) {
			throw new IllegalArgumentException("argument array must be an array");
		}
		Class<?> componentType = clazz.getComponentType();
		if (!componentType.isPrimitive()) {
			return ((Object[]) array)[index];
		}

		return fetchPrimitiveArrayValue(array, index, componentType);
	}

	/**
	 * 提取基本类型数组array[index]的值
	 * 
	 * @param array
	 * @param index
	 * @return
	 */
	private static Object fetchPrimitiveArrayValue(Object array, int index, Class<?> componentType) {
		if (long.class.equals(componentType)) {
			return ((long[]) array)[index];
		}
		if (int.class.equals(componentType)) {
			return ((int[]) array)[index];
		}
		if (boolean.class.equals(componentType)) {
			return ((boolean[]) array)[index];
		}
		if (double.class.equals(componentType)) {
			return ((double[]) array)[index];
		}
		if (char.class.equals(componentType)) {
			return ((char[]) array)[index];
		}
		if (float.class.equals(componentType)) {
			return ((float[]) array)[index];
		}
		if (byte.class.equals(componentType)) {
			return ((byte[]) array)[index];
		}
		if (short.class.equals(componentType)) {
			return ((short[]) array)[index];
		}
		return null;
	}
	
	/**
	 * 获取批量SQL/Shell中需要使用的参数的List
	 * 
	 * @param args
	 * @param batchDefinition
	 * @return
	 */
	public static List<Object[]> generateBatchQueryArguments(Object[] args, BatchDefinition batchDefinition) {
		// 获取批量SQL/Shell的批数，即@BatchParam对应的几个数组的最小length
		int batchSize = -1;
		Integer[] batchParamIndexes = batchDefinition.getBatchParamIndexes();
		for (int i = 0; i < batchParamIndexes.length; i++) {
			int index = batchParamIndexes[i];
			if (batchSize == -1) {
				batchSize = ArrayUtils.getLength(args[index]);
			} else {
				batchSize = Math.min(batchSize, ArrayUtils.getLength(args[index]));
			}
		}

		Method[] getterMethods = batchDefinition.getGetterMethods();
		Integer[] parameterIndexes = batchDefinition.getParameterIndexes();
		List<Object[]> paramArrays = new ArrayList<Object[]>(batchSize);
		List<String> parameterNames = batchDefinition.getParsedExpression().getParameterNames();

		// 每次克隆一组调用参数，然后用批量参数中这个批次中的批量参数值替换这个批量参数
		for (int i = 0; i < batchSize; i++) {
			Object[] cloneArgs = ArrayUtils.clone(args);

			for (int j = 0; j < batchParamIndexes.length; j++) {
				int index = batchParamIndexes[j];
				cloneArgs[index] = fetchArrayValue(cloneArgs[index], i);
			}

			Object[] paramArray = fetchVlaues(getterMethods, parameterIndexes, cloneArgs, parameterNames);
			paramArrays.add(paramArray);
		}

		return paramArrays;
	}
}
