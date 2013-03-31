package net.energy.exception;

/**
 * 当无法访问到Cache，或者Cache操作发生异常时，将抛出此异常
 * 
 * @author wuqh
 * 
 */
public class CacheUnreachableException extends RuntimeException {
	private static final long serialVersionUID = 68138569334466501L;

	public CacheUnreachableException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CacheUnreachableException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public CacheUnreachableException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public CacheUnreachableException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
