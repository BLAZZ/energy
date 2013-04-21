package net.energy.mongo.definition;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.energy.annotation.mongo.MongoUpdate;
import net.energy.exception.DaoGenerateException;
import net.energy.expression.ExpressionParser;
import net.energy.expression.ParsedExpression;
import net.energy.expression.ParserFacotory;
import net.energy.expression.ParserFacotory.ExpressionType;
import net.energy.utils.CommonUtils;

/**
 * 通过对配置了@MongoUpdate方法的解析， 产生需要在执行Mongo更新操作时必要用到的参数。
 * 
 * @author wuqh
 * 
 */
public class MongoUpdateDefinition extends BaseMongoDefinition {
	private ParsedExpression parsedModifierShell;
	private String modifierShellWithToken;
	private Method[] modifierGetterMethods;
	private Integer[] modifierParameterIndexes;
	private boolean upsert;
	private boolean multi;

	public MongoUpdateDefinition(Method method) throws DaoGenerateException {
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

		parseParameterAnnotations(method, annotations, paramIndexes, null, paramTypes);

		MongoUpdate updateShell = method.getAnnotation(MongoUpdate.class);
		String queryShell = updateShell.query();

		upsert = updateShell.upsert();
		multi = updateShell.multi();

		// 解析查询的Shell语句和更新的Shell语句
		parseShell(queryShell, paramTypes, paramIndexes, null);
		parseModifierShell(updateShell.modifier(), paramTypes, paramIndexes);
	}

	/**
	 * 检查加了@MongoUpdate注解的方法的返回值类型:必须返回void或者boolean
	 * 
	 * @param method
	 * @throws DaoGenerateException
	 */
	private void checkReturnType(Method method) throws DaoGenerateException {
		checkUpsetReturnType(method, "MongoInsert");
	}

	/**
	 * 解析更新操作shell的方法，和<code>parseShell</code>类似
	 * 
	 * @param modifierShell
	 * @param paramTypes
	 * @param paramIndexes
	 * @throws DaoGenerateException
	 */
	private void parseModifierShell(String modifierShell, Class<?>[] paramTypes, Map<String, Integer> paramIndexes)
			throws DaoGenerateException {
		ExpressionParser parser = ParserFacotory.createExpressionParser(ExpressionType.MONGO_SHELL);
		parsedModifierShell = parser.parse(modifierShell);
		List<String> parameterNames = parsedModifierShell.getParameterNames();
		Object[] gettersAndIndexes = null;
		gettersAndIndexes = CommonUtils.getGettersAndIndexes(parameterNames, paramIndexes, paramTypes);
		Method[] getterMethods = (Method[]) gettersAndIndexes[0];
		Integer[] parameterIndexes = (Integer[]) gettersAndIndexes[1];

		this.modifierGetterMethods = getterMethods;
		this.modifierParameterIndexes = parameterIndexes;
		this.modifierShellWithToken = CommonUtils.getShell(parsedModifierShell);
	}

	public Method[] getModifierGetterMethods() {
		return modifierGetterMethods;
	}

	public Integer[] getModifierParameterIndexes() {
		return modifierParameterIndexes;
	}

	public boolean isUpsert() {
		return upsert;
	}

	public boolean isMulti() {
		return multi;
	}

	public String getModifierShellWithToken() {
		return modifierShellWithToken;
	}

	public ParsedExpression getParsedModifierShell() {
		return parsedModifierShell;
	}
}
