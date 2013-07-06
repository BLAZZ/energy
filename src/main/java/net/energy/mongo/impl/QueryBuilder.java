package net.energy.mongo.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.energy.exception.IllegalMongoShellException;
import net.energy.utils.ExpressionUtils;

import org.bson.types.BSONTimestamp;
import org.bson.types.Binary;
import org.bson.types.Code;
import org.bson.types.CodeWScope;
import org.bson.types.MaxKey;
import org.bson.types.MinKey;
import org.bson.types.ObjectId;

import com.mongodb.DBObject;
import com.mongodb.DBRefBase;
import com.mongodb.util.JSON;

/**
 * 用于MongoDB的Shell和数据绑定
 * 
 * @author wuqh
 * 
 */
public class QueryBuilder {
	private static final Set<Class<?>> PRIMITIVES;
	private static final Pattern PATTERN;
	private static final String TOKEN;

	static {
		PRIMITIVES = new HashSet<Class<?>>();
		PRIMITIVES.add(String.class);
		PRIMITIVES.add(Number.class);
		PRIMITIVES.add(Boolean.class);
		PRIMITIVES.add(MinKey.class);
		PRIMITIVES.add(MaxKey.class);
		PRIMITIVES.add(ObjectId.class);
		PRIMITIVES.add(Pattern.class);
		PRIMITIVES.add(BSONTimestamp.class);
		PRIMITIVES.add(Date.class);
		PRIMITIVES.add(UUID.class);
		PRIMITIVES.add(Code.class);
		PRIMITIVES.add(DBObject.class);
		PRIMITIVES.add(DBRefBase.class);
		PRIMITIVES.add(CodeWScope.class);
		PRIMITIVES.add(Binary.class);

		TOKEN = ExpressionUtils.SHELL_TOKEN;
		PATTERN = Pattern.compile(TOKEN);
	}

	private static <T> boolean isBsonPrimitives(Class<T> clazz) {
		if (PRIMITIVES.contains(clazz)) {
			return true;
		}

		for (Class<?> primitive : PRIMITIVES) {
			if (primitive.isAssignableFrom(clazz)) {
				return true;
			}
		}

		return false;
	}

	public static DBObject toDBObject(String query, Object... parameters) {
		String finalQuery = generateQuery(query, parameters);
		try {
			return (DBObject) JSON.parse(finalQuery);
		} catch (Exception e) {
			throw new IllegalMongoShellException("Mongo Query表达式式非法:", e);
		}
	}

	private static String generateQuery(String query, Object... parameters) {
		assertThatParamsCanBeBound(query, parameters);
		int paramIndex = 0;
		while (query.contains(TOKEN)) {
			Object parameter = parameters[paramIndex++];
			query = bindParamIntoQuery(query, parameter);
		}
		return query;
	}

	private static String bindParamIntoQuery(String query, Object parameter) {

		try {
			String paramAsJson = convertParameter(parameter);
			return query.replaceFirst(TOKEN, getMatcherWithEscapedDollar(paramAsJson));

		} catch (RuntimeException e) {
			return handleInvalidBinding(query, parameter, e);
		}
	}

	private static String convertParameter(Object parameter) {
		if (QueryBuilder.isBsonPrimitives(parameter.getClass())) {
			return convertWithDriver(parameter);
		}
		String message = "无法将非BSON基本类型(non-bson-primitive)参数[" + parameter + "]绑定到查询json字符串中";
		throw new IllegalMongoShellException(message);
	}

	private static String convertWithDriver(Object parameter) {
		try {
			return JSON.serialize(parameter);
		} catch (Exception e) {
			String message = "无法将参数[" + parameter + "]绑定到查询json字符串中";
			throw new IllegalMongoShellException(message, e);
		}
	}

	private static String handleInvalidBinding(String query, Object parameter, RuntimeException e) {
		String message = "绑定参数[" + parameter + "]到查询语句[" + query + "]失败";
		throw new IllegalMongoShellException(message, e);
	}

	private static void assertThatParamsCanBeBound(String template, Object[] parameters) {
		int nbTokens = countTokens(template);
		if (nbTokens != parameters.length) {
			String message = "绑定参数到查询语句[" + template + "]失败：参数个数[" + parameters.length + "]和Shell中Token个数[" + nbTokens
					+ "]不匹配";

			throw new IllegalMongoShellException(message);
		}
	}

	private static String getMatcherWithEscapedDollar(String serialized) {
		return Matcher.quoteReplacement(serialized);
	}

	private static int countTokens(String template) {
		int count = 0;
		Matcher matcher = PATTERN.matcher(template);
		while (matcher.find()) {
			count++;
		}
		return count;
	}
}
