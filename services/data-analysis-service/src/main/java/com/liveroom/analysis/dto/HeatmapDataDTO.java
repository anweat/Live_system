package com.liveroom.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 时段热力图DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapDataDTO {
    
    /** X轴数据（日期或小时） */
    private List<String> xAxis;
    
    /** Y轴数据（星期或时段） */
    private List<String> yAxis;
    
    /** 热力图数据矩阵 [[x, y, value], ...] */
    private List<List<Object>> data;
    
    /** 最大值 */
    private Double maxValue;
    
    /** 最小值 */
    private Double minValue;
    
    /** 数据类型（hourly/weekly/tag） */
    private String dataType;
}
