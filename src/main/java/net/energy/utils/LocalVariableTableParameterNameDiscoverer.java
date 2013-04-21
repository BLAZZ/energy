package net.energy.utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * {@link ParameterNameDiscoverer}
 * 的默认实现类，使用Java字节码的LocalVariableTable信息获取参数名。如果".class"文件不包含debug信息（参数名称在
 * ".class"的attribute_info的LocalVariableTable中）则返回 <code>null</code>
 * 
 * <p>
 * 使用ASM框架解析class文件来获取方法名称。
 * 
 */
public class LocalVariableTableParameterNameDiscoverer implements ParameterNameDiscoverer {

	private static Log LOGGER = LogFactory.getLog(LocalVariableTableParameterNameDiscoverer.class);

	// marker object for classes that do not have any debug info
	private static final Map<Member, String[]> NO_DEBUG_INFO_MAP = Collections.emptyMap();

	// the cache uses a nested index (value is a map) to keep the top level
	// cache relatively small in size
	private final Map<Class<?>, Map<Member, String[]>> parameterNamesCache = new ConcurrentHashMap<Class<?>, Map<Member, String[]>>();

	public String[] getParameterNames(Method method) {
		Class<?> declaringClass = method.getDeclaringClass();
		Map<Member, String[]> map = this.parameterNamesCache.get(declaringClass);
		if (map == null) {
			// initialize cache
			map = inspectClass(declaringClass);
			this.parameterNamesCache.put(declaringClass, map);
		}
		if (map != NO_DEBUG_INFO_MAP) {
			return map.get(method);
		}
		return null;
	}

	public String[] getParameterNames(Constructor<?> constructor) {
		Class<?> declaringClass = constructor.getDeclaringClass();
		Map<Member, String[]> map = this.parameterNamesCache.get(declaringClass);
		if (map == null) {
			// initialize cache
			map = inspectClass(declaringClass);
			this.parameterNamesCache.put(declaringClass, map);
		}
		if (map != NO_DEBUG_INFO_MAP) {
			return map.get(constructor);
		}

		return null;
	}

	/**
	 * Inspects the target class. Exceptions will be logged and a maker map
	 * returned to indicate the lack of debug information.
	 */
	private Map<Member, String[]> inspectClass(Class<?> clazz) {
		InputStream is = clazz.getResourceAsStream(CommonUtils.getClassFileName(clazz));
		if (is == null) {
			// We couldn't load the class file, which is not fatal as it
			// simply means this method of discovering parameter names won't
			// work.
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Cannot find '.class' file for class [" + clazz
						+ "] - unable to determine constructors/methods parameter names");
			}
			return NO_DEBUG_INFO_MAP;
		}
		try {
			ClassReader classReader = new ClassReader(is);
			Map<Member, String[]> map = new ConcurrentHashMap<Member, String[]>();
			classReader.accept(new ParameterNameDiscoveringVisitor(clazz, map), ClassReader.SKIP_FRAMES);
			return map;
		} catch (IOException ex) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Exception thrown while reading '.class' file for class [" + clazz
						+ "] - unable to determine constructors/methods parameter names", ex);
			}
		} finally {
			try {
				is.close();
			} catch (IOException ex) {
				// ignore
			}
		}
		return NO_DEBUG_INFO_MAP;
	}

	/**
	 * Helper class that inspects all methods (constructor included) and then
	 * attempts to find the parameter names for that member.
	 */
	private static class ParameterNameDiscoveringVisitor extends EmptyVisitor {

		private static final String STATIC_CLASS_INIT = "<clinit>";

		private final Class<?> clazz;
		private final Map<Member, String[]> memberMap;

		public ParameterNameDiscoveringVisitor(Class<?> clazz, Map<Member, String[]> memberMap) {
			this.clazz = clazz;
			this.memberMap = memberMap;
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
			// exclude synthetic + bridged && static class initialization
			if (!isSyntheticOrBridged(access) && !STATIC_CLASS_INIT.equals(name)) {
				return new LocalVariableTableVisitor(clazz, memberMap, name, desc, isStatic(access));
			}
			return null;
		}

		private static boolean isSyntheticOrBridged(int access) {
			return (((access & Opcodes.ACC_SYNTHETIC) | (access & Opcodes.ACC_BRIDGE)) > 0);
		}

		private static boolean isStatic(int access) {
			return ((access & Opcodes.ACC_STATIC) > 0);
		}
	}

	private static class LocalVariableTableVisitor extends EmptyVisitor {

		private static final String CONSTRUCTOR = "<init>";

		private final Class<?> clazz;
		private final Map<Member, String[]> memberMap;
		private final String name;
		private final Type[] args;
		private final boolean isStatic;

		private String[] parameterNames;
		private boolean hasLvtInfo = false;

		/*
		 * The nth entry contains the slot index of the LVT table entry holding
		 * the argument name for the nth parameter.
		 */
		private final int[] lvtSlotIndex;

		public LocalVariableTableVisitor(Class<?> clazz, Map<Member, String[]> map, String name, String desc,
				boolean isStatic) {
			this.clazz = clazz;
			this.memberMap = map;
			this.name = name;
			// determine args
			args = Type.getArgumentTypes(desc);
			this.parameterNames = new String[args.length];
			this.isStatic = isStatic;
			this.lvtSlotIndex = computeLvtSlotIndices(isStatic, args);
		}

		@Override
		public void visitLocalVariable(String name, String description, String signature, Label start, Label end,
				int index) {
			this.hasLvtInfo = true;
			for (int i = 0; i < lvtSlotIndex.length; i++) {
				if (lvtSlotIndex[i] == index) {
					this.parameterNames[i] = name;
				}
			}
		}

		@Override
		public void visitEnd() {
			if (this.hasLvtInfo || (this.isStatic && this.parameterNames.length == 0)) {
				// visitLocalVariable will never be called for static no args
				// methods
				// which doesn't use any local variables.
				// This means that hasLvtInfo could be false for that kind of
				// methods
				// even if the class has local variable info.
				memberMap.put(resolveMember(), parameterNames);
			}
		}

		private Member resolveMember() {
			ClassLoader loader = clazz.getClassLoader();
			Class<?>[] classes = new Class<?>[args.length];

			// resolve args
			for (int i = 0; i < args.length; i++) {
				String className = null;
				try {
					className = args[i].getClassName();
					classes[i] = ClassUtils.getClass(loader, className);
				} catch (ClassNotFoundException e) {
					throw new IllegalArgumentException("Cannot find class [" + className + "]", e);
				}
			}
			try {
				if (CONSTRUCTOR.equals(name)) {
					return clazz.getDeclaredConstructor(classes);
				}

				return clazz.getDeclaredMethod(name, classes);
			} catch (NoSuchMethodException ex) {
				throw new IllegalStateException("Method [" + name
						+ "] was discovered in the .class file but cannot be resolved in the class object", ex);
			}
		}

		private static int[] computeLvtSlotIndices(boolean isStatic, Type[] paramTypes) {
			int[] lvtIndex = new int[paramTypes.length];
			int nextIndex = (isStatic ? 0 : 1);
			for (int i = 0; i < paramTypes.length; i++) {
				lvtIndex[i] = nextIndex;
				if (isWideType(paramTypes[i])) {
					nextIndex += 2;
				} else {
					nextIndex++;
				}
			}
			return lvtIndex;
		}

		private static boolean isWideType(Type aType) {
			// float is not a wide type
			return (aType == Type.LONG_TYPE || aType == Type.DOUBLE_TYPE);
		}
	}

}
