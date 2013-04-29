package net.energy.exception;

/**
 * DAO方法配置异常，通常是Annotation误用导致的异常。
 * 
 * @author wuqh
 * 
 */
public class DaoGenerateException extends Exception {
	private static final long serialVersionUID = -6330147888709232917L;

	public DaoGenerateException() {
		super();
		
	}

	public DaoGenerateException(String message, Throwable cause) {
		super(message, cause);
		
	}

	public DaoGenerateException(String message) {
		super(message);
		
	}

	public DaoGenerateException(Throwable cause) {
		super(cause);
		
	}

}
