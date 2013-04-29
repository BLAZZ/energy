package net.energy.definition.mongo;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.energy.annotation.mongo.MongoUpdate;
import net.energy.exception.DaoGenerateException;
import net.energy.expression.ExpressionParser;
import net.energy.expression.ParsedExpression;
import net.energy.expression.ParserFacotory;
import net.energy.expression.ParserFacotory.ExpressionType;
import net.energy.utils.ExpressionUtils;
import net.energy.utils.ReflectionUtils;

/**
 * 通过对配置了@MongoUpdate方法的解析， 产生需要在执行Mongo更新操作时必要用到的参数。
 * 
 * @author wuqh
 * 
 */
public class MongoUpdateDefinition extends BaseMongoDefinition {
	private static final Log LOGGER = LogFactory.getLog(MongoUpdateDefinition.class);
	private ParsedExpression parsedModifierShell;
	private String modifierShellWithToken;
	private Method[] modifierGetterMethods;
	private Integer[] modifierParameterIndexes;
	private boolean upsert;
	private boolean multi;

	public MongoUpdateDefinition(Method method) throws DaoGenerateException {
		super(method);
	}

	@Override
	protected String getSourceShell(Method method) {
		MongoUpdate updateShell = method.getAnnotation(MongoUpdate.class);
		return updateShell.query();
	}

	@Override
	protected void parseInternal(Method method, Map<String, Integer> paramIndexes,
			Map<String, Integer> batchParamIndexes) throws DaoGenerateException {
		super.parseInternal(method, paramIndexes, batchParamIndexes);

		MongoUpdate updateShell = method.getAnnotation(MongoUpdate.class);
		upsert = updateShell.upsert();
		multi = updateShell.multi();

		Class<?>[] paramTypes = method.getParameterTypes();
		parseModifierShell(updateShell.modifier(), paramTypes, paramIndexes);
	}

	@Override
	protected void checkBeforeParse(Method method) throws DaoGenerateException {
		super.checkBeforeParse(method);
		checkUpsetReturnType(method, "MongoUpdate");
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
		gettersAndIndexes = ReflectionUtils.getGettersAndIndexes(parameterNames, paramIndexes, paramTypes);
		Method[] getterMethods = (Method[]) gettersAndIndexes[0];
		Integer[] parameterIndexes = (Integer[]) gettersAndIndexes[1];

		this.modifierGetterMethods = getterMethods;
		this.modifierParameterIndexes = parameterIndexes;
		this.modifierShellWithToken = ExpressionUtils.getShell(parsedModifierShell);
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

	@Override
	protected void logBindInfo(Method method) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("绑定" + getDescription() + "到方法[" + method + "]成功");
		}
	}

	private String getDescription() {
		String desc = "@MongoUpdate(query=[" + this.getParsedShell().getOriginalExpression() + "],modifier=["
				+ this.getParsedModifierShell().getOriginalExpression() + "],upsert=[" + this.isUpsert() + "],multi=["
				+ this.isMulti() + "])";

		if (!StringUtils.isEmpty(globalCollectionName)) {
			desc = ",@MongoCollection(" + globalCollectionName + ")";
		}

		return desc;
	}
}
