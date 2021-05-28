package com.albedo.java.modules.quartz.util;

import com.albedo.java.modules.quartz.domain.Job;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;

/**
 * 定时任务处理（禁止并发执行）
 *
 * @author somewhere
 */
@DisallowConcurrentExecution
public class QuartzDisallowConcurrentExecution extends AbstractQuartzJob {

	@Override
	protected void doExecute(JobExecutionContext context, Job job) throws Exception {
		JobInvokeUtil.invokeMethod(job);
	}

}
