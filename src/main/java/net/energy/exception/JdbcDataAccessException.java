package net.energy.exception;

/**
 * JDBC数据访问异常
 * 
 * @author wuqh
 * 
 */
public class JdbcDataAccessException extends DataAccessException {
	private static final long serialVersionUID = 1173641670975074781L;

	public JdbcDataAccessException() {
		super();
		
	}

	public JdbcDataAccessException(String message, Throwable cause) {
		super(message, cause);
		
	}

	public JdbcDataAccessException(String message) {
		super(message);
		
	}

	public JdbcDataAccessException(Throwable cause) {
		super(cause);
		
	}

}
