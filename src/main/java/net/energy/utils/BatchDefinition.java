package net.energy.utils;

import java.lang.reflect.Method;

import net.energy.expression.ParsedExpression;

/**
 * 批量操作配置接口
 * 
 * @author wuqh
 * 
 */
public interface BatchDefinition {

	/**
	 * 由于批量执行过程中，需要逐一替换参数中对映位置的值，所以需要记录每一个@BatchParam在args中的index
	 * 
	 * <pre>
	 * 例如：
	 * <code>@BatchUpdate("insert into photo(ownerId, albumId, file) values (:user.id, :album.id, :photo.file)")</code>
	 * <code>@ReturnId
	 * public List<Long> insertPhotosReturnIds(@Param("user") User user, @Param("album") Album album, @BatchParam("photo") Photo[] photo);</code>
	 * 对应的batchParamIndexes就会是[3]
	 * </pre>
	 * 
	 * @return
	 */
	Integer[] getBatchParamIndexes();

	/**
	 * 用于从方法参数的Bean对象中获取需要变量值的getter方法。缓存起来用于减少反射的查询操作
	 * 
	 * @return
	 */
	Method[] getGetterMethods();

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
	 * 因为，args[]=[1,2,[3,4]]这样的参数在调用时会转换为[[1,2,3],[1,2,4]]给BatchSQL调用。
	 * 所以，对于@BatchParam中的每一个值，在实际调用过程中都相当于每次都是一个@Param
	 * @return
	 */
	Integer[] getParameterIndexes();

	/**
	 * 获取解析后的表达式
	 * 
	 * @return
	 */
	ParsedExpression getParsedExpression();

}
