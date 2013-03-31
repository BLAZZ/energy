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
		// TODO Auto-generated constructor stub
	}

	public CountRowNumberException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public CountRowNumberException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public CountRowNumberException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
