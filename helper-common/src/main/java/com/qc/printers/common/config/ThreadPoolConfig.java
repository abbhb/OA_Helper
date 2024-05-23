package com.qc.printers.common.config;

import com.qc.printers.common.common.factory.MyThreadFactory;
import com.qc.printers.transaction.annotation.SecureInvokeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Description: 线程池配置
 * Author: <a href="https://github.com/zongzibinbin">abin</a>
 * Date: 2023-04-09
 */
@Configuration
@EnableAsync
public class ThreadPoolConfig implements AsyncConfigurer, SecureInvokeConfigurer {
    /**
     * 项目共用线程池
     */
    public static final String EASYOA_EXECUTOR = "easyoaExecutor";
    /**
     * websocket通信线程池
     */
    public static final String WS_EXECUTOR = "websocketExecutor";


    public static final String AICHAT_EXECUTOR = "aichatExecutor";

    @Override
    public Executor getAsyncExecutor() {
        return easyoaExecutor();
    }

    @Override
    public Executor getSecureInvokeExecutor() {
        return easyoaExecutor();
    }

    @Bean(EASYOA_EXECUTOR)
    @Primary // 该注解可以在很多相同类型的Bean提供优先级
    public ThreadPoolTaskExecutor easyoaExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        executor.setCorePoolSize(10);
        // 设置最大线程数
        executor.setMaxPoolSize(10);
        // 设置队列容量
        executor.setQueueCapacity(200);
        // 设置线程名称前缀
        executor.setThreadNamePrefix("easyoa-executor-");
        // 设置拒绝策略，当线程池满时，调用者线程来执行任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());//满了调用线程执行，认为重要任务
        // 设置线程工厂，这里是使用了一个自定义的 MyThreadFactory
        executor.setThreadFactory(new MyThreadFactory(executor));
        // 初始化线程池
        executor.initialize();
        return executor;
    }

    @Bean(WS_EXECUTOR)
    public ThreadPoolTaskExecutor websocketExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(16);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(1000);//支持同时推送1000人
        executor.setThreadNamePrefix("websocket-executor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());//满了直接丢弃，默认为不重要消息推送
        executor.setThreadFactory(new MyThreadFactory(executor));
        executor.initialize();
        return executor;
    }

    @Bean(AICHAT_EXECUTOR)
    public ThreadPoolTaskExecutor chatAiExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(15);
        executor.setThreadNamePrefix("aichat-executor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());//满了直接丢弃，默认为不重要消息推送
        executor.setThreadFactory(new MyThreadFactory(executor));
        return executor;
    }
}
