package net.energy.jdbc.impl;

import javax.sql.DataSource;

import net.energy.jdbc.Dialect;
import net.energy.jdbc.JdbcDataAccessor;
import net.energy.queue.Task;
import net.energy.queue.TaskParameters;
import net.energy.queue.TaskRepository;

/**
 * JdbcDataAccessor的高并发实现。采用队列方式，多线程共享数据，以牺牲一定数据一致性为代价，获得较高的并发处理性能。
 * 
 * @author wuqh
 * 
 */
public class QueuedJdbcDataAccessor extends SimpleJdbcDataAccessor implements JdbcDataAccessor {
	//private static final Log LOGGER = LogFactory.getLog(QueuedJdbcDataAccessor.class);
	private SimpleJdbcDataAccessor dataAccessor = new SimpleJdbcDataAccessor();
	
	private long timeout = 0L;
	
	@Override
	protected <T> T execute(String sql, int fetchSize, boolean returnKeys, PreparedStatementCallback<T> action,
			Object... args) {
		if ((action instanceof QueryCallback<?>) == false) {
			return dataAccessor.execute(sql, fetchSize, returnKeys, action, args);
		}
		
		TaskParameters parameters = new JdbcQueryTaskParameters(sql, fetchSize, returnKeys, action, args);
		
		Task task = new JdbcQueryTask(dataAccessor);
		
		TaskRepository.addTask(task, parameters);
		
		T result = TaskRepository.processTask(task, parameters, timeout);
		
		return result;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	@Override
	public void setDataSource(DataSource dataSource) {
		// TODO Auto-generated method stub
		dataAccessor.setDataSource(dataSource);
	}

	@Override
	public void setDialect(Dialect dialect) {
		// TODO Auto-generated method stub
		dataAccessor.setDialect(dialect);
	}

}
