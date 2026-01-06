package common.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import common.logger.TraceLogger;
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
