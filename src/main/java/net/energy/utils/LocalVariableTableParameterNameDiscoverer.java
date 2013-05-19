package net.energy.utils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

	// 用于标记没有DEBUG信息（DEBUG和CODE信息）的类文件
	private static final Map<Member, String[]> NO_DEBUG_INFO_MAP = Collections.emptyMap();

	// 方法名缓存，缓存采用class级别缓存，里面包含方法和对应参数的映射
	private final Map<Class<?>, Map<Member, String[]>> parameterNamesCache = new ConcurrentHashMap<Class<?>, Map<Member, String[]>>();

	public String[] getParameterNames(Method method) {
		Class<?> declaringClass = method.getDeclaringClass();
		Map<Member, String[]> map = this.parameterNamesCache.get(declaringClass);
		if (map == null) {
			// 初始化Cache
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
			// 初始化Cache
			map = inspectClass(declaringClass);
			this.parameterNamesCache.put(declaringClass, map);
		}
		if (map != NO_DEBUG_INFO_MAP) {
			return map.get(constructor);
		}

		return null;
	}

	/**
	 * 检查Class. 读取".class"文件，并解析方法以及对应的参数名数组，如果不包含DEBUG信息则返回空的Map
	 * 
	 * @param clazz
	 */
	private Map<Member, String[]> inspectClass(Class<?> clazz) {
		InputStream is = clazz.getResourceAsStream(ClassHelper.getClassFileName(clazz));
		if (is == null) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("无法找到[" + clazz + "]的'.class'文件 ，所以无法获取构造方法/方法中的参数名");
			}
			return NO_DEBUG_INFO_MAP;
		}
		try {
			ClassReader classReader = new ClassReader(is);
			Map<Member, String[]> map = new ConcurrentHashMap<Member, String[]>();
			// 需要使用大于SKIP_DEBUG和SKIP_CODE的级别，用于获取DEBUG信息
			classReader.accept(new ParameterNameDiscoveringVisitor(clazz, map), ClassReader.SKIP_FRAMES);
			return map;
		} catch (IOException ex) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("读取[" + clazz + "]的'.class'文件 发生异常，所以无法获取构造方法/方法中的参数名", ex);
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
	 * 参数名获取器，用于访问所有方法（包括构造方法）来获取方法的参数名
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
			// 过滤synthetic（非用户代码产生）、 bridged、static类的构造方法
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

	/**
	 * LVT（Local Variable Table）访问器，用于访问方法的LVT
	 * 
	 *
	 */
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
		 * 第N个方法参数的slot索引，lvtSlotIndex[0]表示第一个参数的slot索引
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
				//由于静态的无参数方法不需要本地变量，所以不会有hasLvtInfo，需要特殊处理
				memberMap.put(resolveMember(), parameterNames);
			}
		}

		private Member resolveMember() {
			ClassLoader loader = clazz.getClassLoader();
			Class<?>[] classes = new Class<?>[args.length];

			// 解析参数
			for (int i = 0; i < args.length; i++) {
				String className = null;
				try {
					className = args[i].getClassName();
					classes[i] = ClassHelper.getClass(loader, className);
				} catch (ClassNotFoundException e) {
					throw new IllegalArgumentException("找不到类[" + className + "]", e);
				}
			}
			try {
				if (CONSTRUCTOR.equals(name)) {
					return clazz.getDeclaredConstructor(classes);
				}

				return clazz.getDeclaredMethod(name, classes);
			} catch (NoSuchMethodException ex) {
				throw new IllegalStateException(".class文件中的方法 [" + name + "]无法在类对象中找到", ex);
			}
		}

		private static int[] computeLvtSlotIndices(boolean isStatic, Type[] paramTypes) {
			int[] lvtIndex = new int[paramTypes.length];
			int nextIndex = (isStatic ? 0 : 1);
			// 非静态方法的第一个参数是this
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
			// 是否为宽类型（Long和Double），宽类型占两个slot
			return (aType == Type.LONG_TYPE || aType == Type.DOUBLE_TYPE);
		}
	}

}
