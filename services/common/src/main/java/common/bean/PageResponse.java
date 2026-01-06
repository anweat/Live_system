package common.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应对象
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
    private int pageNum;

    /** 每页记录数 */
    private int pageSize;

    /** 总页数 */
    private int totalPages;

    /** 是否有下一页 */
    private boolean hasNextPage;

    /** 是否有上一页 */
    private boolean hasPreviousPage;

    /**
     * 创建分页响应
     */
    public static <T> PageResponse<T> of(List<T> items, long total, int pageNum, int pageSize) {
        int totalPages = (int) ((total + pageSize - 1) / pageSize);
        boolean hasNextPage = pageNum < totalPages;
        boolean hasPreviousPage = pageNum > 1;

        return PageResponse.<T>builder()
                .items(items)
                .total(total)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .totalPages(totalPages)
                .hasNextPage(hasNextPage)
                .hasPreviousPage(hasPreviousPage)
                .build();
    }

    /**
     * 创建空分页响应
     */
    public static <T> PageResponse<T> empty() {
        return PageResponse.<T>builder()
                .items(List.of())
                .total(0)
                .pageNum(1)
                .pageSize(10)
                .totalPages(0)
                .hasNextPage(false)
                .hasPreviousPage(false)
                .build();
    }
}
