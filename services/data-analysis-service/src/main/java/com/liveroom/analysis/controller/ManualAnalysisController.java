package com.liveroom.analysis.controller;

import com.liveroom.analysis.service.ManualAnalysisService;
import common.bean.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 手动数据分析Controller
 * 提供手动触发分析、自定义查询、数据导入等功能
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/analysis/manual")
public class ManualAnalysisController {

    @Autowired
    private ManualAnalysisService manualAnalysisService;

    /**
     * 执行自定义SQL查询分析
     *
     * @param sql SQL查询语句
     * @param datasource 数据源（db1或db2）
     * @return 查询结果
     */
    @PostMapping("/query")
    public Result<Map<String, Object>> executeCustomQuery(
            @RequestParam String sql,
            @RequestParam(defaultValue = "db1") String datasource) {
        log.info("执行自定义查询: datasource={}, sql={}", datasource, sql);
        Map<String, Object> result = manualAnalysisService.executeCustomQuery(sql, datasource);
        return Result.ok(result);
    }

    /**
     * 手动触发全量数据同步
     *
     * @return 同步结果
     */
    @PostMapping("/sync/full")
    public Result<Map<String, Object>> triggerFullSync() {
        log.info("手动触发全量数据同步");
        Map<String, Object> result = manualAnalysisService.triggerFullDataSync();
        return Result.ok(result);
    }

    /**
     * 手动触发增量数据同步
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 同步结果
     */
    @PostMapping("/sync/incremental")
    public Result<Map<String, Object>> triggerIncrementalSync(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        log.info("手动触发增量数据同步: startDate={}, endDate={}", startDate, endDate);
        Map<String, Object> result = manualAnalysisService.triggerIncrementalSync(startDate, endDate);
        return Result.ok(result);
    }

    /**
     * 自定义时间范围分析
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param metrics 分析指标（逗号分隔：gmv,users,transactions等）
     * @return 分析结果
     */
    @GetMapping("/custom-range")
    public Result<Map<String, Object>> customRangeAnalysis(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam String metrics) {
        log.info("自定义时间范围分析: startDate={}, endDate={}, metrics={}", 
                startDate, endDate, metrics);
        Map<String, Object> result = manualAnalysisService.customRangeAnalysis(
                startDate, endDate, metrics);
        return Result.ok(result);
    }

    /**
     * 对比分析（任意两个时间段）
     *
     * @param period1Start 时期1开始
     * @param period1End 时期1结束
     * @param period2Start 时期2开始
     * @param period2End 时期2结束
     * @return 对比结果
     */
    @GetMapping("/compare-periods")
    public Result<Map<String, Object>> comparePeriods(
            @RequestParam String period1Start,
            @RequestParam String period1End,
            @RequestParam String period2Start,
            @RequestParam String period2End) {
        log.info("对比分析: period1=[{},{}], period2=[{},{}]",
                period1Start, period1End, period2Start, period2End);
        Map<String, Object> result = manualAnalysisService.comparePeriods(
                period1Start, period1End, period2Start, period2End);
        return Result.ok(result);
    }

    /**
     * 用户群体分析（自定义条件）
     *
     * @param conditions 筛选条件JSON
     * @return 分析结果
     */
    @PostMapping("/cohort-analysis")
    public Result<Map<String, Object>> cohortAnalysis(@RequestBody Map<String, Object> conditions) {
        log.info("用户群体分析: conditions={}", conditions);
        Map<String, Object> result = manualAnalysisService.cohortAnalysis(conditions);
        return Result.ok(result);
    }

    /**
     * 主播分组对比分析
     *
     * @param anchorIds1 主播组1（逗号分隔）
     * @param anchorIds2 主播组2（逗号分隔）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 对比结果
     */
    @GetMapping("/compare-anchor-groups")
    public Result<Map<String, Object>> compareAnchorGroups(
            @RequestParam String anchorIds1,
            @RequestParam String anchorIds2,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        log.info("主播分组对比: group1={}, group2={}, date=[{},{}]",
                anchorIds1, anchorIds2, startDate, endDate);
        Map<String, Object> result = manualAnalysisService.compareAnchorGroups(
                anchorIds1, anchorIds2, startDate, endDate);
        return Result.ok(result);
    }

    /**
     * 漏斗分析（自定义步骤）
     *
     * @param steps 漏斗步骤配置
     * @return 漏斗分析结果
     */
    @PostMapping("/funnel-analysis")
    public Result<Map<String, Object>> funnelAnalysis(@RequestBody Map<String, Object> steps) {
        log.info("漏斗分析: steps={}", steps);
        Map<String, Object> result = manualAnalysisService.funnelAnalysis(steps);
        return Result.ok(result);
    }

    /**
     * 异常检测分析
     *
     * @param metric 检测指标（gmv/users/transactions等）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param threshold 阈值倍数（默认3倍标准差）
     * @return 异常点列表
     */
    @GetMapping("/anomaly-detection")
    public Result<Map<String, Object>> anomalyDetection(
            @RequestParam String metric,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "3.0") Double threshold) {
        log.info("异常检测: metric={}, date=[{},{}], threshold={}",
                metric, startDate, endDate, threshold);
        Map<String, Object> result = manualAnalysisService.anomalyDetection(
                metric, startDate, endDate, threshold);
        return Result.ok(result);
    }

    /**
     * 相关性分析
     *
     * @param metric1 指标1
     * @param metric2 指标2
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 相关系数和分析
     */
    @GetMapping("/correlation-analysis")
    public Result<Map<String, Object>> correlationAnalysis(
            @RequestParam String metric1,
            @RequestParam String metric2,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        log.info("相关性分析: metric1={}, metric2={}, date=[{},{}]",
                metric1, metric2, startDate, endDate);
        Map<String, Object> result = manualAnalysisService.correlationAnalysis(
                metric1, metric2, startDate, endDate);
        return Result.ok(result);
    }

    /**
     * 导入CSV数据进行分析
     *
     * @param file CSV文件
     * @param analysisType 分析类型
     * @return 分析结果
     */
    @PostMapping("/import-csv")
    public Result<Map<String, Object>> importCsvData(
            @RequestParam("file") MultipartFile file,
            @RequestParam String analysisType) {
        log.info("导入CSV数据: filename={}, analysisType={}", file.getOriginalFilename(), analysisType);
        Map<String, Object> result = manualAnalysisService.importCsvData(file, analysisType);
        return Result.ok(result);
    }

    /**
     * 生成自定义报表
     *
     * @param config 报表配置
     * @return 报表数据和下载链接
     */
    @PostMapping("/generate-report")
    public Result<Map<String, Object>> generateCustomReport(@RequestBody Map<String, Object> config) {
        log.info("生成自定义报表: config={}", config);
        Map<String, Object> result = manualAnalysisService.generateCustomReport(config);
        return Result.ok(result);
    }

    /**
     * 预测分析
     *
     * @param metric 预测指标
     * @param days 预测天数
     * @param model 预测模型（linear/arima/prophet）
     * @return 预测结果
     */
    @GetMapping("/prediction")
    public Result<Map<String, Object>> prediction(
            @RequestParam String metric,
            @RequestParam(defaultValue = "7") Integer days,
            @RequestParam(defaultValue = "linear") String model) {
        log.info("预测分析: metric={}, days={}, model={}", metric, days, model);
        Map<String, Object> result = manualAnalysisService.prediction(metric, days, model);
        return Result.ok(result);
    }

    /**
     * 清除指定缓存
     *
     * @param pattern 缓存键模式（支持通配符）
     * @return 清除结果
     */
    @DeleteMapping("/cache")
    public Result<Map<String, Object>> clearCache(@RequestParam String pattern) {
        log.info("清除缓存: pattern={}", pattern);
        Map<String, Object> result = manualAnalysisService.clearCache(pattern);
        return Result.ok(result);
    }

    /**
     * 数据质量检查
     *
     * @param datasource 数据源
     * @param table 表名（可选）
     * @return 质量检查报告
     */
    @GetMapping("/data-quality-check")
    public Result<Map<String, Object>> dataQualityCheck(
            @RequestParam String datasource,
            @RequestParam(required = false) String table) {
        log.info("数据质量检查: datasource={}, table={}", datasource, table);
        Map<String, Object> result = manualAnalysisService.dataQualityCheck(datasource, table);
        return Result.ok(result);
    }
}
