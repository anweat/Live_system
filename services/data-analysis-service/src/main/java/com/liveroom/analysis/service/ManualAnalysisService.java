package com.liveroom.analysis.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 手动数据分析Service接口
 */
public interface ManualAnalysisService {

    /**
     * 执行自定义SQL查询
     *
     * @param sql SQL查询语句
     * @param datasource 数据源（db1或db2）
     * @return 查询结果
     */
    Map<String, Object> executeCustomQuery(String sql, String datasource);

    /**
     * 触发全量数据同步
     *
     * @return 同步结果
     */
    Map<String, Object> triggerFullDataSync();

    /**
     * 触发增量数据同步
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 同步结果
     */
    Map<String, Object> triggerIncrementalSync(String startDate, String endDate);

    /**
     * 自定义时间范围分析
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param metrics 分析指标
     * @return 分析结果
     */
    Map<String, Object> customRangeAnalysis(String startDate, String endDate, String metrics);

    /**
     * 时间段对比分析
     *
     * @param period1Start 时期1开始
     * @param period1End 时期1结束
     * @param period2Start 时期2开始
     * @param period2End 时期2结束
     * @return 对比结果
     */
    Map<String, Object> comparePeriods(String period1Start, String period1End,
                                       String period2Start, String period2End);

    /**
     * 用户群体分析
     *
     * @param conditions 筛选条件
     * @return 分析结果
     */
    Map<String, Object> cohortAnalysis(Map<String, Object> conditions);

    /**
     * 主播分组对比分析
     *
     * @param anchorIds1 主播组1
     * @param anchorIds2 主播组2
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 对比结果
     */
    Map<String, Object> compareAnchorGroups(String anchorIds1, String anchorIds2,
                                           String startDate, String endDate);

    /**
     * 漏斗分析
     *
     * @param steps 漏斗步骤配置
     * @return 漏斗分析结果
     */
    Map<String, Object> funnelAnalysis(Map<String, Object> steps);

    /**
     * 异常检测分析
     *
     * @param metric 检测指标
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param threshold 阈值倍数
     * @return 异常点列表
     */
    Map<String, Object> anomalyDetection(String metric, String startDate,
                                        String endDate, Double threshold);

    /**
     * 相关性分析
     *
     * @param metric1 指标1
     * @param metric2 指标2
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 相关系数和分析
     */
    Map<String, Object> correlationAnalysis(String metric1, String metric2,
                                           String startDate, String endDate);

    /**
     * 导入CSV数据进行分析
     *
     * @param file CSV文件
     * @param analysisType 分析类型
     * @return 分析结果
     */
    Map<String, Object> importCsvData(MultipartFile file, String analysisType);

    /**
     * 生成自定义报表
     *
     * @param config 报表配置
     * @return 报表数据
     */
    Map<String, Object> generateCustomReport(Map<String, Object> config);

    /**
     * 预测分析
     *
     * @param metric 预测指标
     * @param days 预测天数
     * @param model 预测模型
     * @return 预测结果
     */
    Map<String, Object> prediction(String metric, Integer days, String model);

    /**
     * 清除缓存
     *
     * @param pattern 缓存键模式
     * @return 清除结果
     */
    Map<String, Object> clearCache(String pattern);

    /**
     * 数据质量检查
     *
     * @param datasource 数据源
     * @param table 表名
     * @return 质量检查报告
     */
    Map<String, Object> dataQualityCheck(String datasource, String table);
}
