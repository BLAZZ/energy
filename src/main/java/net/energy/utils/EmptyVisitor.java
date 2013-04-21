package net.energy.utils;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class EmptyVisitor implements ClassVisitor, FieldVisitor, MethodVisitor,AnnotationVisitor {

	public void visit(String arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}

	public AnnotationVisitor visitAnnotation(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public AnnotationVisitor visitArray(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void visitEnum(String arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

	public AnnotationVisitor visitAnnotationDefault() {
		// TODO Auto-generated method stub
		return null;
	}

	public void visitCode() {
		// TODO Auto-generated method stub
		
	}

	public void visitFieldInsn(int arg0, String arg1, String arg2, String arg3) {
		// TODO Auto-generated method stub
		
	}

	public void visitFrame(int arg0, int arg1, Object[] arg2, int arg3, Object[] arg4) {
		// TODO Auto-generated method stub
		
	}

	public void visitIincInsn(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	public void visitInsn(int arg0) {
		// TODO Auto-generated method stub
		
	}

	public void visitIntInsn(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	public void visitJumpInsn(int arg0, Label arg1) {
		// TODO Auto-generated method stub
		
	}

	public void visitLabel(Label arg0) {
		// TODO Auto-generated method stub
		
	}

	public void visitLdcInsn(Object arg0) {
		// TODO Auto-generated method stub
		
	}

	public void visitLineNumber(int arg0, Label arg1) {
		// TODO Auto-generated method stub
		
	}

	public void visitLocalVariable(String arg0, String arg1, String arg2, Label arg3, Label arg4, int arg5) {
		// TODO Auto-generated method stub
		
	}

	public void visitLookupSwitchInsn(Label arg0, int[] arg1, Label[] arg2) {
		// TODO Auto-generated method stub
		
	}

	public void visitMaxs(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	public void visitMethodInsn(int arg0, String arg1, String arg2, String arg3) {
		// TODO Auto-generated method stub
		
	}

	public void visitMultiANewArrayInsn(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	public AnnotationVisitor visitParameterAnnotation(int arg0, String arg1, boolean arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	public void visitTableSwitchInsn(int arg0, int arg1, Label arg2, Label[] arg3) {
		// TODO Auto-generated method stub
		
	}

	public void visitTryCatchBlock(Label arg0, Label arg1, Label arg2, String arg3) {
		// TODO Auto-generated method stub
		
	}

	public void visitTypeInsn(int arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	public void visitVarInsn(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	public void visit(int arg0, int arg1, String arg2, String arg3, String arg4, String[] arg5) {
		// TODO Auto-generated method stub
		
	}

	public AnnotationVisitor visitAnnotation(String arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public void visitAttribute(Attribute arg0) {
		// TODO Auto-generated method stub
		
	}

	public void visitEnd() {
		// TODO Auto-generated method stub
		
	}

	public FieldVisitor visitField(int arg0, String arg1, String arg2, String arg3, Object arg4) {
		// TODO Auto-generated method stub
		return null;
	}

	public void visitInnerClass(String arg0, String arg1, String arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	public MethodVisitor visitMethod(int arg0, String arg1, String arg2, String arg3, String[] arg4) {
		// TODO Auto-generated method stub
		return null;
	}

	public void visitOuterClass(String arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

	public void visitSource(String arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

}
