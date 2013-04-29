package net.energy.utils;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * 
 * 空的ASM各类访问器实现，用于进行适配
 * 
 * @author wuqh
 *
 */
public class EmptyVisitor implements ClassVisitor, FieldVisitor, MethodVisitor,AnnotationVisitor {

	public void visit(String arg0, Object arg1) {
		
		
	}

	public AnnotationVisitor visitAnnotation(String arg0, String arg1) {
		
		return null;
	}

	public AnnotationVisitor visitArray(String arg0) {
		
		return null;
	}

	public void visitEnum(String arg0, String arg1, String arg2) {
		
		
	}

	public AnnotationVisitor visitAnnotationDefault() {
		
		return null;
	}

	public void visitCode() {
		
		
	}

	public void visitFieldInsn(int arg0, String arg1, String arg2, String arg3) {
		
		
	}

	public void visitFrame(int arg0, int arg1, Object[] arg2, int arg3, Object[] arg4) {
		
		
	}

	public void visitIincInsn(int arg0, int arg1) {
		
		
	}

	public void visitInsn(int arg0) {
		
		
	}

	public void visitIntInsn(int arg0, int arg1) {
		
		
	}

	public void visitJumpInsn(int arg0, Label arg1) {
		
		
	}

	public void visitLabel(Label arg0) {
		
		
	}

	public void visitLdcInsn(Object arg0) {
		
		
	}

	public void visitLineNumber(int arg0, Label arg1) {
		
		
	}

	public void visitLocalVariable(String arg0, String arg1, String arg2, Label arg3, Label arg4, int arg5) {
		
		
	}

	public void visitLookupSwitchInsn(Label arg0, int[] arg1, Label[] arg2) {
		
		
	}

	public void visitMaxs(int arg0, int arg1) {
		
		
	}

	public void visitMethodInsn(int arg0, String arg1, String arg2, String arg3) {
		
		
	}

	public void visitMultiANewArrayInsn(String arg0, int arg1) {
		
		
	}

	public AnnotationVisitor visitParameterAnnotation(int arg0, String arg1, boolean arg2) {
		
		return null;
	}

	public void visitTableSwitchInsn(int arg0, int arg1, Label arg2, Label[] arg3) {
		
		
	}

	public void visitTryCatchBlock(Label arg0, Label arg1, Label arg2, String arg3) {
		
		
	}

	public void visitTypeInsn(int arg0, String arg1) {
		
		
	}

	public void visitVarInsn(int arg0, int arg1) {
		
		
	}

	public void visit(int arg0, int arg1, String arg2, String arg3, String arg4, String[] arg5) {
		
		
	}

	public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
		
		return null;
	}

	public void visitAttribute(Attribute arg0) {
		
		
	}

	public void visitEnd() {
		
		
	}

	public FieldVisitor visitField(int arg0, String arg1, String arg2, String arg3, Object arg4) {
		
		return null;
	}

	public void visitInnerClass(String arg0, String arg1, String arg2, int arg3) {
		
		
	}

	public MethodVisitor visitMethod(int arg0, String arg1, String arg2, String arg3, String[] arg4) {
		
		return null;
	}

	public void visitOuterClass(String arg0, String arg1, String arg2) {
		
		
	}

	public void visitSource(String arg0, String arg1) {
		
		
	}

}
