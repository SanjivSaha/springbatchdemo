package com.demo.batch.tasklet;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.demo.batch.constants.BatchConstants;
import com.demo.batch.model.EmployeeModel;
import com.demo.batch.model.JobExecutionModel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.opencsv.CSVWriter;

@Component
public class JobProcessor implements Tasklet,IJobProcessor {

	@Autowired
	Environment env;

	@Autowired
	JobExecutionModel jobExecutionModel;

	@Autowired
	RestTemplate restTemplate;

	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public RepeatStatus execute(StepContribution arg0, ChunkContext arg1) throws Exception {
		try
		{
			preProcess(jobExecutionModel);
			process(jobExecutionModel);
			postProcess(jobExecutionModel);
		}
		catch(Exception e)
		{
			logger.info(e.getMessage());
			throw e;
		}
		return RepeatStatus.FINISHED;
	}

	@Override
	public void preProcess(JobExecutionModel jobExecutionModel) throws Exception 
	{
		try
		{
			logger.info("PreProcess start");
			String baseFileName = env.getProperty(BatchConstants.BASEFILENAME);
			String ext = env.getProperty(BatchConstants.EXTENSION);
			String pattern = BatchConstants.DATEPATTERN;
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String date = simpleDateFormat.format(new Date());
			StringBuilder stringBuilder = new StringBuilder();
			String filename= stringBuilder.append(baseFileName).append("_").append(date).append(".").append(ext).toString();
			jobExecutionModel.setFileName(filename);
			logger.info("PreProcess end");
        }
		catch(Exception e)
		{
			logger.info(e.getMessage());
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public void process(JobExecutionModel jobExecutionModel) throws Exception {
		try
		{
			logger.info("Process start");
			String resourceUrl = BatchConstants.RESOURCEURL;
			//EmployeeModel emp= restTemplate.getForObject(resourceUrl, EmployeeModel.class,64);
			ResponseEntity<List<EmployeeModel>> responseEntity =
					restTemplate.exchange(resourceUrl,
							HttpMethod.GET, null, new ParameterizedTypeReference<List<EmployeeModel>>() {
					});
			List<EmployeeModel> listOfString = responseEntity.getBody();
			Map<String,Object> resultSet = new HashMap<String,Object>();
			resultSet.put("resultSet",listOfString);
			jobExecutionModel.setSessionData(resultSet);
			writeToCsv(jobExecutionModel);
			logger.info("Process end");
		}
		catch(Exception e)
		{
			logger.info(e.getMessage());
			throw e;
		}
	}

	@Override
	public void postProcess(JobExecutionModel jobExecutionModel) throws Exception {
		try
		{
			logger.info("PostProcess start");
			String fileName = jobExecutionModel.getFileName();
			performSftp(fileName);
			logger.info("PostProcess end");
		}
		catch(Exception e)
		{
			logger.info(e.getMessage());
		}
	}

	public void writeToCsv(JobExecutionModel jobExecutionModel) throws Exception {
		CSVWriter csvWriter = new CSVWriter(new FileWriter(jobExecutionModel.getFileName()));
		EmployeeModel employeeModelObj = new EmployeeModel();
		try
		{
			List<EmployeeModel> emp = (List<EmployeeModel>)jobExecutionModel.getSessionData().get("resultSet");
			Iterator<EmployeeModel> itr = emp.iterator();
			while(itr.hasNext())
			{
				employeeModelObj=itr.next();
				csvWriter.writeNext(new String[]{employeeModelObj.getId(),employeeModelObj.getEmployee_name(), 
						employeeModelObj.getEmployee_age(),employeeModelObj.getEmployee_salary(),
						employeeModelObj.getProfile_image()
				});
				employeeModelObj = new EmployeeModel();
			}
		}
		catch(Exception e)
		{
			logger.info(e.getMessage());
			throw e;
		}
		finally 
		{
			csvWriter.close();
		}
	}

	public void performSftp(String fileName) throws Exception{
		try
		{
			logger.info("Sftp start");
			String sftpId = env.getProperty(BatchConstants.SFTPUSERID);
			String sftpPwd = env.getProperty(BatchConstants.SFTPPASSWORD);
			String sftpHost = env.getProperty(BatchConstants.SFTPHOST);
			Integer sftpPort = Integer.parseInt(env.getProperty(BatchConstants.SFTPPORT));
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", BatchConstants.NO); //Should be enabled for better authentication
			JSch jsch = new JSch(); 
			Session session = jsch.getSession(sftpId, sftpHost, sftpPort);
			session.setConfig(config);
			session.setPassword(sftpPwd);
			session.connect();
			ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
			sftpChannel.connect();
            sftpChannel.put(BatchConstants.SRCLOC+fileName, "/"+fileName);
			sftpChannel.disconnect();
			session.disconnect();
			logger.info("Sftp end");;
		}
		catch(Exception e)
		{
			logger.info(e.getMessage());
			throw e;
		}
	}

}
