package net.energy.jdbc.impl;

import net.energy.queue.Task;
import net.energy.queue.TaskParameters;
import net.energy.queue.TaskResult;
import net.energy.utils.Assert;

/**
 * JDBC查询任务
 * 
 * @author wuqh
 * @see Task
 */
public class JdbcQueryTask extends Task {
	private final SimpleJdbcDataAccessor dataAccessor;
	
	public JdbcQueryTask(SimpleJdbcDataAccessor dataAccessor) {
		Assert.isNull(dataAccessor, "DataAccessorObject can't be null");
		this.dataAccessor = dataAccessor;
	}
	
	@SuppressWarnings("unchecked")
	public <T> TaskResult<T> process(TaskParameters parameters) {
		JdbcQueryTaskParameters taskParameters = (JdbcQueryTaskParameters) parameters;
		
		String sql = taskParameters.getSql();
		int fetchSize = taskParameters.getFetchSize();
		boolean returnKeys = taskParameters.isReturnKeys();
		PreparedStatementCallback<T> action = (PreparedStatementCallback<T>) taskParameters.getAction();
		Object[] args = taskParameters.getArgs();
		
		T result = dataAccessor.execute(sql, fetchSize, returnKeys, action, args);
		
		TaskResult<T> taskResult = new TaskResult<T>();
		taskResult.setResult(result);
		taskResult.setDone(true);
		
		return taskResult;
	}
	
}
