package com.liveroom.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 时间序列数据DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSeriesDataDTO {
    
    /** 时间点列表 */
    private List<String> timePoints;
    
    /** 数值列表 */
    private List<BigDecimal> values;
    
    /** 数据类型（daily/hourly/weekly/monthly） */
    private String dataType;
    
    /** 开始时间 */
    private LocalDateTime startTime;
    
    /** 结束时间 */
    private LocalDateTime endTime;
    
    /** 总计 */
    private BigDecimal total;
    
    /** 平均值 */
    private BigDecimal average;
    
    /** 最大值 */
    private BigDecimal maxValue;
    
    /** 最小值 */
    private BigDecimal minValue;
}
