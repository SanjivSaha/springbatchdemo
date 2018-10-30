package com.demo.batch.model;

import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class JobExecutionModel {

private String jobName;
private String fileName;
private String ext;
private String smtpReceipient;
private Map<String,Object> sessionData;

public String getJobName() {
	return jobName;
}
public void setJobName(String jobName) {
	this.jobName = jobName;
}
public String getFileName() {
	return fileName;
}
public void setFileName(String fileName) {
	this.fileName = fileName;
}
public String getExt() {
	return ext;
}
public void setExt(String ext) {
	this.ext = ext;
}
public String getSmtpReceipient() {
	return smtpReceipient;
}
public void setSmtpReceipient(String smtpReceipient) {
	this.smtpReceipient = smtpReceipient;
}
public Map<String,Object> getSessionData() {
	return sessionData;
}
public void setSessionData(Map<String,Object> sessionData) {
	this.sessionData = sessionData;
}



}
