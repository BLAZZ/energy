package net.energy.utils;

import java.lang.reflect.Array;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

/**
 * 数组工具类
 * 
 * @author wuqh
 *
 */
public class ArrayHelper extends ArrayUtils {

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
	
	public static int getArrayOrListLength(Object arrayOrList) {
		if (arrayOrList == null) {
			return 0;
		}

		Class<?> clazz = arrayOrList.getClass();
		boolean isArray = ClassHelper.isTypeArray(clazz);
		boolean isList = ClassHelper.isTypeList(clazz);
		
		
		if (!isArray && !isList) {
			throw new IllegalArgumentException("arrayOrList参数必须为数组或者List的实现类");
		}
		
		if(isArray) {
			return getLength(arrayOrList);
		} else {
			return ((List<?>) arrayOrList).size();
		}
	}

}
