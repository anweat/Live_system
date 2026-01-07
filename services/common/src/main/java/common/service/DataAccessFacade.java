package common.service;

import common.service.*;
import common.service.query.RankingQueryService;
import common.service.query.TagAnalysisQueryService;
import common.service.query.FinancialAnalysisQueryService;
import common.service.query.RetentionAnalysisQueryService;
import common.service.query.HeatmapAnalysisQueryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import common.logger.TraceLogger;
import common.service.query.TimeSeriesQueryService;
import common.service.query.AggregationQueryService;
import common.service.query.SegmentationQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 数据访问门面(DataAccessFacade)
 * 
 * 统一的数据库访问接口，所有其他微服务模块都必须通过此门面来访问数据库
 * 这样可以确保：
 * 1. 数据访问的统一性和一致性
 * 2. 缓存策略的统一实施
 * 3. 数据格式的统一管理
 * 4. 事务的统一控制
 * 5. 批量操作的统一优化
 * 
 * 使用方式示例：
 * 
 * @Autowired
 * private DataAccessFacade dataAccessFacade;
 * 
 * // 用户相关操作
 * User user = dataAccessFacade.user().findById(userId).orElse(null);
 * List<User> users = dataAccessFacade.user().findAll();
 * 
 * // 主播相关操作
 * Anchor anchor = dataAccessFacade.anchor().findByUserId(userId).orElse(null);
 * 
 * // 打赏相关操作
 * Recharge recharge = dataAccessFacade.recharge().createRecharge(rechargeRecord);
 * 
 * // 结算相关操作
 * Settlement settlement = dataAccessFacade.settlement().findByAnchorId(anchorId).orElse(null);
 *
 * // 数据分析查询（新增）
 * TimeSeriesData dailyData = dataAccessFacade.timeSeriesQuery().getDailyTimeSeries(...);
 * RankingData topPayers = dataAccessFacade.rankingQuery().getTopPayersByAnchor(...);
 * KeyMetrics metrics = dataAccessFacade.aggregationQuery().getKeyMetrics(...);
 * ConsumptionSegmentation segments = dataAccessFacade.segmentationQuery().getConsumptionSegmentation(...);
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataAccessFacade {

    private final UserService userService;
    private final AnchorService anchorService;
    private final AudienceService audienceService;
    private final LiveRoomService liveRoomService;
    private final RechargeService rechargeService;
    private final SettlementService settlementService;
    private final WithdrawalService withdrawalService;
    private final CommissionRateService commissionRateService;
    private final SyncProgressService syncProgressService;
    private final MessageService messageService;
    private final LiveRoomRealtimeService liveRoomRealtimeService;
    private final AnalysisQueryService analysisQueryService;
    private final TimeSeriesQueryService timeSeriesQueryService;
    private final RankingQueryService rankingQueryService;
    private final AggregationQueryService aggregationQueryService;
    private final SegmentationQueryService segmentationQueryService;
    private final TagAnalysisQueryService tagAnalysisQueryService;
    private final FinancialAnalysisQueryService financialAnalysisQueryService;
    private final RetentionAnalysisQueryService retentionAnalysisQueryService;
    private final HeatmapAnalysisQueryService heatmapAnalysisQueryService;

    /**
     * 获取用户Service
     */
    public UserService user() {
        TraceLogger.debug("DataAccessFacade", "user", "获取用户Service");
        return userService;
    }

    /**
     * 获取主播Service
     */
    public AnchorService anchor() {
        TraceLogger.debug("DataAccessFacade", "anchor", "获取主播Service");
        return anchorService;
    }

    /**
     * 获取观众Service
     */
    public AudienceService audience() {
        TraceLogger.debug("DataAccessFacade", "audience", "获取观众Service");
        return audienceService;
    }

    /**
     * 获取直播间Service
     */
    public LiveRoomService liveRoom() {
        TraceLogger.debug("DataAccessFacade", "liveRoom", "获取直播间Service");
        return liveRoomService;
    }

    /**
     * 获取打赏Service
     */
    public RechargeService recharge() {
        TraceLogger.debug("DataAccessFacade", "recharge", "获取打赏Service");
        return rechargeService;
    }

    /**
     * 获取结算Service
     */
    public SettlementService settlement() {
        TraceLogger.debug("DataAccessFacade", "settlement", "获取结算Service");
        return settlementService;
    }

    /**
     * 获取提现Service
     */
    public WithdrawalService withdrawal() {
        TraceLogger.debug("DataAccessFacade", "withdrawal", "获取提现Service");
        return withdrawalService;
    }

    /**
     * 获取分成比例Service
     */
    public CommissionRateService commissionRate() {
        TraceLogger.debug("DataAccessFacade", "commissionRate", "获取分成比例Service");
        return commissionRateService;
    }

    /**
     * 获取同步进度Service
     */
    public SyncProgressService syncProgress() {
        TraceLogger.debug("DataAccessFacade", "syncProgress", "获取同步进度Service");
        return syncProgressService;
    }

    /**
     * 获取弹幕消息Service
     */
    public MessageService message() {
        TraceLogger.debug("DataAccessFacade", "message", "获取弹幕消息Service");
        return messageService;
    }

    /**
     * 获取直播间实时数据Service
     */
    public LiveRoomRealtimeService liveRoomRealtime() {
        TraceLogger.debug("DataAccessFacade", "liveRoomRealtime", "获取直播间实时数据Service");
        return liveRoomRealtimeService;
    }

    /**
     * 获取分析查询Service（用于数据分析模块）
     * 提供基础的数据查询能力
     */
    public AnalysisQueryService analysisQuery() {
        TraceLogger.debug("DataAccessFacade", "analysisQuery", "获取分析查询Service");
        return analysisQueryService;
    }

    /**
     * 获取时间序列查询Service
     * 用于处理基于时间维度的数据查询和分析
     */
    public TimeSeriesQueryService timeSeriesQuery() {
        TraceLogger.debug("DataAccessFacade", "timeSeriesQuery", "获取时间序列查询Service");
        return timeSeriesQueryService;
    }

    /**
     * 获取排行榜查询Service
     * 用于处理TOP排行、排序相关的数据查询
     */
    public RankingQueryService rankingQuery() {
        TraceLogger.debug("DataAccessFacade", "rankingQuery", "获取排行榜查询Service");
        return rankingQueryService;
    }

    /**
     * 获取聚合统计查询Service
     * 用于处理各种维度的聚合统计
     */
    public AggregationQueryService aggregationQuery() {
        TraceLogger.debug("DataAccessFacade", "aggregationQuery", "获取聚合统计查询Service");
        return aggregationQueryService;
    }

    /**
     * 获取分段分层查询Service
     * 用于处理数据的分段和分层分析
     */
    public SegmentationQueryService segmentationQuery() {
        TraceLogger.debug("DataAccessFacade", "segmentationQuery", "获取分段分层查询Service");
        return segmentationQueryService;
    }

    /**
     * 获取标签分析查询Service
     * 用于处理标签关联度、热力图等分析
     */
    public TagAnalysisQueryService tagAnalysisQuery() {
        TraceLogger.debug("DataAccessFacade", "tagAnalysisQuery", "获取标签分析查询Service");
        return tagAnalysisQueryService;
    }

    /**
     * 获取财务分析查询Service
     * 用于处理GMV、ARPU、ARPPU等财务指标分析
     */
    public FinancialAnalysisQueryService financialAnalysisQuery() {
        TraceLogger.debug("DataAccessFacade", "financialAnalysisQuery", "获取财务分析查询Service");
        return financialAnalysisQueryService;
    }

    /**
     * 获取用户留存分析查询Service
     * 用于处理留存率、流失预警等分析
     */
    public RetentionAnalysisQueryService retentionAnalysisQuery() {
        TraceLogger.debug("DataAccessFacade", "retentionAnalysisQuery", "获取用户留存分析查询Service");
        return retentionAnalysisQueryService;
    }

    /**
     * 获取热力图分析查询Service
     * 用于处理时段热力图、活跃度热力图等分析
     */
    public HeatmapAnalysisQueryService heatmapAnalysisQuery() {
        TraceLogger.debug("DataAccessFacade", "heatmapAnalysisQuery", "获取热力图分析查询Service");
        return heatmapAnalysisQueryService;
    }

    /**
     * 开始事务批量操作
     * 示例：使用此方法在一个事务内完成多个操作
     *
     * dataAccessFacade.beginBatchOperation(facade -> {
     *     // 批量创建用户
     *     List<User> users = ...;
     *     facade.user().batchSaveUsers(users);
     *
     *     // 批量创建观众
     *     List<Audience> audiences = ...;
     *     facade.audience().batchSaveAudience(audiences);
     * });
     */
    @Transactional
    public void beginBatchOperation(java.util.function.Consumer<DataAccessFacade> operation) {
        try {
            operation.accept(this);
            TraceLogger.info("DataAccessFacade", "beginBatchOperation", "批量操作完成");
        } catch (Exception e) {
            TraceLogger.error("DataAccessFacade", "beginBatchOperation", "批量操作失败", e);
            throw new RuntimeException("批量操作失败", e);
        }
    }

    /**
     * 检查数据库连接是否正常
     */
    public boolean healthCheck() {
        try {
            long userCount = userService.count();
            TraceLogger.info("DataAccessFacade", "healthCheck", "健康检查通过，用户总数: " + userCount);
            return true;
        } catch (Exception e) {
            TraceLogger.error("DataAccessFacade", "healthCheck", "健康检查失败", e);
            return false;
        }
    }

    /**
     * 清空所有缓存
     * 用于定期维护或数据同步时的缓存清理
     */
    public void clearAllCache() {
        TraceLogger.info("DataAccessFacade", "clearAllCache", "开始清空所有缓存");
        try {
            // 缓存清理由Spring的CacheEvict注解自动处理
            // 这里只是记录日志
            log.warn("缓存清空请求已记录，具体清理由CacheManager执行");
        } catch (Exception e) {
            TraceLogger.error("DataAccessFacade", "clearAllCache", "清空缓存失败", e);
            throw new RuntimeException("清空缓存失败", e);
        }
    }
}
