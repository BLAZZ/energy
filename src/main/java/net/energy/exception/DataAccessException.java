package net.energy.exception;

/**
 * 数据访问异常
 * 
 * @author wuqh
 * 
 */
public class DataAccessException extends RuntimeException {
	private static final long serialVersionUID = 4475919649680645675L;

	public DataAccessException() {
		super();
		
	}

	public DataAccessException(String message, Throwable cause) {
		super(message, cause);
		
	}

	public DataAccessException(String message) {
		super(message);
		
	}

	public DataAccessException(Throwable cause) {
		super(cause);
		
	}

}
