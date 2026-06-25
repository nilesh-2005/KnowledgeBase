package com.nilesh.knowledgebase.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("doc-proc-");
        executor.setRejectedExecutionHandler((runnable, exec) ->
                log.error("[AsyncConfig] Task rejected from thread pool - pool is saturated. " +
                          "Active={}, Queue={}/{}",
                          exec.getActiveCount(),
                          exec.getQueue().size(),
                          exec.getQueue().remainingCapacity()));
        executor.initialize();
        return executor;
    }
}
