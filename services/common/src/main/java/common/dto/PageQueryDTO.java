package common.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import common.constant.SystemConstants;

/**
 * 分页查询请求DTO
 * 用于分页查询的基础参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageQueryDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    /** 当前页码（从1开始） */
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNo;

    /** 每页大小 */
    @Min(value = 1, message = "每页大小最小为1")
    @Max(value = 1000, message = "每页大小最大为1000")
    private Integer pageSize;

    /** 排序字段 */
    private String orderBy;

    /** 排序方向: ASC/DESC */
    private String direction;

    /**
     * 初始化默认值
     */
    public void initDefaults() {
        if (pageNo == null || pageNo < 1) {
            pageNo = SystemConstants.DEFAULT_PAGE_NO;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = SystemConstants.DEFAULT_PAGE_SIZE;
        }
        if (pageSize > SystemConstants.MAX_PAGE_SIZE) {
            pageSize = SystemConstants.MAX_PAGE_SIZE;
        }
        if (direction == null) {
            direction = "DESC";
        }
    }

    /**
     * 获取数据库查询的偏移量
     */
    public int getOffset() {
        return (pageNo - 1) * pageSize;
    }

    /**
     * 验证分页查询DTO的有效性
     */
    public boolean validate() {
        if (pageNo == null || pageNo < 1) {
            return false;
        }
        if (pageSize == null || pageSize < 1 || pageSize > SystemConstants.MAX_PAGE_SIZE) {
            return false;
        }
        if (direction != null && !direction.equalsIgnoreCase("ASC") && !direction.equalsIgnoreCase("DESC")) {
            return false;
        }
        return true;
    }
}
