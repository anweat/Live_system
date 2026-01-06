/**
 * ⚠️ 已废弃 - DEPRECATED
 * 
 * 该包中的所有 MyBatis Mapper 类已被废弃，不再使用。
 * 请使用新的 Repository + Service 架构。
 * 
 * 迁移指南请参考：docs/迁移指南.md
 * 
 * ===========================================================================
 * DEPRECATION NOTICE - 重要提示
 * ===========================================================================
 * 
 * 为什么废弃？
 * -----------
 * 旧的 MyBatis Mapper 模式存在以下问题：
 * 1. 无法自动管理缓存 - 每个 Mapper 需要手动编写缓存逻辑
 * 2. 代码重复 - 重复的 CRUD 操作、批量操作逻辑
 * 3. 事务管理不统一 - 容易出现事务边界错误
 * 4. 无法实现幂等性 - 容易产生重复操作
 * 5. 不利于系统集成 - 无法全局管控数据访问
 * 
 * 新架构优势
 * -----------
 * 新的 Repository + Service + Facade 架构：
 * ✅ 自动缓存管理（@Cacheable / @CacheEvict）
 * ✅ 通用 CRUD 操作（BaseService 提供）
 * ✅ 统一事务管理（@Transactional）
 * ✅ 幂等性控制（traceId 防重）
 * ✅ 批量操作支持（自动分批 500 条）
 * ✅ 全局数据访问管控（DataAccessFacade）
 * 
 * 当前状态
 * -----------
 * 这些 Mapper 文件仍然存在，但已不再被使用。
 * 后续可能会被移到 deprecated/ 目录或删除。
 * 
 * 迁移步骤
 * -----------
 * 1. 不要在新代码中使用 Mapper
 * 2. 使用 DataAccessFacade 访问所有数据
 * 3. 如需迁移现有代码，参考 docs/迁移指南.md
 * 4. 文件列表：
 *    - UserMapper.java → 使用 UserService
 *    - AnchorMapper.java → 使用 AnchorService
 *    - AudienceMapper.java → 使用 AudienceService
 *    - LiveRoomMapper.java → 使用 LiveRoomService
 *    - RechargeMapper.java → 使用 RechargeService
 *    - SettlementMapper.java → 使用 SettlementService （如存在）
 *    - CommissionRateMapper.java → 使用 CommissionRateService
 *    - WithdrawalMapper.java → 使用 WithdrawalService
 *    - SyncProgressMapper.java → 使用 SyncProgressService
 *    - TagMapper.java → 使用 TagService （如存在）
 *    - TagRelationMapper.java → 使用 TagRelationService （如存在）
 * 
 * 查找包含使用
 * -----------
 * 要找到仍在使用这些 Mapper 的代码：
 * 
 * grep -r "import.*mapper" --include="*.java" src/
 * grep -r "@Autowired.*Mapper" --include="*.java" src/
 * 
 * 例如：
 * @Autowired
 * private UserMapper userMapper;    // ❌ 这是旧代码
 * 
 * 应改为：
 * @Autowired
 * private DataAccessFacade facade;  // ✅ 这是新代码
 * facade.user().findById(id);
 * 
 * 常见问题
 * -----------
 * Q: 可以同时使用 Mapper 和 Service 吗？
 * A: 不建议。会造成缓存不一致，可能导致数据问题。
 * 
 * Q: Mapper 什么时候会被删除？
 * A: 当所有代码都迁移到新架构后，会删除。
 * 
 * Q: 如何检查是否还有代码使用 Mapper？
 * A: 使用 IDE 的 Find Usages 功能或 grep 命令。
 * 
 * ===========================================================================
 * 更多信息
 * ===========================================================================
 * 
 * 文档位置：services/common/docs/
 * - 功能文档.md ..................... 架构设计
 * - 接口文档.md ..................... API 参考
 * - 迁移指南.md ..................... 迁移步骤
 * - 数据访问层使用指南.md ......... 详细使用教程
 * 
 * Repository 位置：services/common/src/main/java/common/repository/
 * Service 位置：services/common/src/main/java/common/service/
 * 
 * ===========================================================================
 */
