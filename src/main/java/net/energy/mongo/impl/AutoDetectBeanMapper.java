package net.energy.mongo.impl;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Pattern;

import net.energy.exception.DaoGenerateException;
import net.energy.exception.DataAccessException;
import net.energy.mongo.BeanMapper;
import net.energy.utils.BeanAutoDetect;
import net.energy.utils.ClassHelper;
import net.sf.cglib.core.ReflectUtils;

import org.bson.types.BSONTimestamp;
import org.bson.types.Code;
import org.bson.types.ObjectId;

import com.mongodb.DBObject;

public class AutoDetectBeanMapper<T> extends BeanAutoDetect implements BeanMapper<T> {
	public AutoDetectBeanMapper(Class<T> clazz) throws DaoGenerateException {
		super(clazz);
	}

	@SuppressWarnings("unchecked")
	@Override
	public T mapper(DBObject o) {
		T result = (T) ReflectUtils.newInstance(clazz);
		
		if (result == null) {
			return result;
		}

		Map<String, Object> resultMap = o.toMap();

		for (Entry<String, Object> entry : resultMap.entrySet()) {
			String key = entry.getKey();
			Object val = entry.getValue();
			Method writeMethod = writeMethods.get(key);
			try {
				writeMethod.invoke(result, convert(val, key));
			} catch (Exception e) {
				throw new DataAccessException("映射数据失败:", e);
			}
		}

		return result;
	}

	@Override
	public boolean isAllowAutoMapType(Class<?> clazz) {
		return clazz.isPrimitive() || ClassHelper.wrapperToPrimitive(clazz) != null || clazz.equals(String.class)
				|| clazz.isEnum() || clazz.equals(BigDecimal.class) || clazz.equals(Number.class)
				|| clazz.equals(BigDecimal.class) || Date.class.isAssignableFrom(clazz) || clazz.equals(byte[].class)
				|| clazz.equals(UUID.class) || clazz.equals(Code.class) || clazz.equals(BSONTimestamp.class)
				|| clazz.equals(ObjectId.class) || clazz.equals(Pattern.class);
	}

}
