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
		// TODO Auto-generated constructor stub
	}

	public DataAccessException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public DataAccessException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public DataAccessException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
