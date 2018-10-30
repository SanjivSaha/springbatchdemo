package com.demo.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.demo.batch.tasklet.JobProcessor;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
 
	@Autowired
	JobProcessor jobProcessor;
	
    @Autowired
    public JobBuilderFactory jobBuilderFactory;
 
    @Autowired
    public StepBuilderFactory stepBuilderFactory;
 
    @Bean
    public Job job() {
        return jobBuilderFactory.get("job")
                .incrementer(new RunIdIncrementer())
                .start(stepOne())
                .build();
    }
 
    @Bean
    public Step stepOne() {
        return stepBuilderFactory.get("stepOne")
        		.tasklet(jobProcessor)
                .build();
    }
}