package net.energy.exception;

/**
 * 统计总记录数时发生的异常
 * 
 * @author wuqh
 * 
 */
public class CountRowNumberException extends DataAccessException {
	private static final long serialVersionUID = 7773212044919145600L;

	public CountRowNumberException() {
		super();
		
	}

	public CountRowNumberException(String message, Throwable cause) {
		super(message, cause);
		
	}

	public CountRowNumberException(String message) {
		super(message);
		
	}

	public CountRowNumberException(Throwable cause) {
		super(cause);
		
	}

}
