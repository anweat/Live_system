package com.liveroom.analysis.util;

import common.logger.TraceLogger;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * 数据管道工具类
 * 用于处理数据分析中的数据提取、转换、加载(ETL)流程
 */
@Slf4j
public class DataPipelineUtil {

    /**
     * 异步执行数据管道任务
     * 
     * @param taskName 任务名称
     * @param supplier 数据处理函数
     * @param executor 执行器
     * @param <T> 返回类型
     * @return CompletableFuture
     */
    public static <T> CompletableFuture<T> asyncExecute(String taskName, 
                                                       Supplier<T> supplier, 
                                                       Executor executor) {
        TraceLogger.info("DataPipelineUtil", "asyncExecute", null, "taskName", taskName);

        return CompletableFuture.supplyAsync(supplier, executor)
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    TraceLogger.error("DataPipelineUtil", "asyncExecute", taskName, throwable);
                } else {
                    TraceLogger.info("DataPipelineUtil", "asyncExecute", taskName, 
                        "result", result != null ? result.toString() : "null");
                }
            });
    }

    /**
     * 执行数据提取任务
     */
    public static <T> T extractData(String operation, Supplier<T> extractor) {
        TraceLogger.info("DataPipelineUtil", "extractData", null, "operation", operation);
        
        try {
            T result = extractor.get();
            TraceLogger.debug("DataPipelineUtil", "extractData", null, 
                "operation", operation, "resultSize", result != null ? result.toString().length() : 0);
            return result;
        } catch (Exception e) {
            TraceLogger.error("DataPipelineUtil", "extractData", operation, e);
            throw e;
        }
    }

    /**
     * 执行数据转换任务
     */
    public static <T, R> R transformData(String operation, T input, java.util.function.Function<T, R> transformer) {
        TraceLogger.info("DataPipelineUtil", "transformData", null, "operation", operation);
        
        try {
            R result = transformer.apply(input);
            TraceLogger.debug("DataPipelineUtil", "transformData", null, 
                "operation", operation, "inputSize", input != null ? input.toString().length() : 0);
            return result;
        } catch (Exception e) {
            TraceLogger.error("DataPipelineUtil", "transformData", operation, e);
            throw e;
        }
    }

    /**
     * 执行数据加载任务
     */
    public static <T, R> R loadData(String operation, T data, java.util.function.Function<T, R> loader) {
        TraceLogger.info("DataPipelineUtil", "loadData", null, "operation", operation);
        
        try {
            R result = loader.apply(data);
            TraceLogger.debug("DataPipelineUtil", "loadData", null, 
                "operation", operation, "dataSize", data != null ? data.toString().length() : 0);
            return result;
        } catch (Exception e) {
            TraceLogger.error("DataPipelineUtil", "loadData", operation, e);
            throw e;
        }
    }

    /**
     * 执行完整的ETL流程
     */
    public static <E, T, L> L executeETL(String operation, 
                                         Supplier<E> extractor, 
                                         java.util.function.Function<E, T> transformer,
                                         java.util.function.Function<T, L> loader) {
        TraceLogger.info("DataPipelineUtil", "executeETL", null, "operation", operation);
        
        try {
            // 提取数据
            E extracted = extractData(operation + "-extract", extractor);
            
            // 转换数据
            T transformed = transformData(operation + "-transform", extracted, transformer);
            
            // 加载数据
            L loaded = loadData(operation + "-load", transformed, loader);
            
            TraceLogger.info("DataPipelineUtil", "executeETL", null, 
                "operation", operation, "status", "SUCCESS");
            
            return loaded;
        } catch (Exception e) {
            TraceLogger.error("DataPipelineUtil", "executeETL", operation, e);
            throw e;
        }
    }
}