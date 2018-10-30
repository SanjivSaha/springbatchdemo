package com.demo.batch.tasklet;

import com.demo.batch.model.JobExecutionModel;

public interface IJobProcessor {
	
	public void preProcess(JobExecutionModel jobExecutionModel) throws Exception;
	public void process(JobExecutionModel jobExecutionModel) throws Exception;
	public void postProcess(JobExecutionModel jobExecutionModel) throws Exception;

}
