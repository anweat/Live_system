package common.response;

import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页响应体
 * 用于返回分页列表数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 分页数据列表 */
    private List<T> items;

    /** 总记录数 */
    private long total;

    /** 当前页码 */
    private int pageNo;

    /** 每页大小 */
    private int pageSize;

    /** 总页数 */
    private int totalPages;

    /** 是否有下一页 */
    private boolean hasNext;

    /** 是否有上一页 */
    private boolean hasPrevious;

    /** 时间戳 */
    private long timestamp;

    /** traceId (用于问题追踪) */
    private String traceId;

    /**
     * 构造分页响应
     */
    public static <T> PageResponse<T> of(List<T> items, long total, int pageNo, int pageSize) {
        int totalPages = (int) ((total + pageSize - 1) / pageSize);
        boolean hasNext = pageNo < totalPages;
        boolean hasPrevious = pageNo > 1;

        return PageResponse.<T>builder()
                .items(items)
                .total(total)
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .hasNext(hasNext)
                .hasPrevious(hasPrevious)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 构造分页响应（带traceId）
     */
    public static <T> PageResponse<T> of(List<T> items, long total, int pageNo, int pageSize, String traceId) {
        int totalPages = (int) ((total + pageSize - 1) / pageSize);
        boolean hasNext = pageNo < totalPages;
        boolean hasPrevious = pageNo > 1;

        return PageResponse.<T>builder()
                .items(items)
                .total(total)
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .hasNext(hasNext)
                .hasPrevious(hasPrevious)
                .timestamp(System.currentTimeMillis())
                .traceId(traceId)
                .build();
    }

    /**
     * 判断是否是最后一页
     */
    public boolean isLastPage() {
        return pageNo >= totalPages;
    }

    /**
     * 获取当前页的起始记录位置（0-indexed）
     */
    public long getOffset() {
        return (long) (pageNo - 1) * pageSize;
    }
}
