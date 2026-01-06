package common.dto;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO基类
 * 所有DTO都应继承此类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 该字段用于校验DTO数据一致性
     */
    protected transient String validationKey;

    /**
     * 用于标记是否需要执行某些业务逻辑
     */
    protected transient boolean executeFlag;

    /**
     * 获取DTO对应的实体类型名称
     */
    public String getEntityName() {
        String className = this.getClass().getSimpleName();
        return className.replaceAll("DTO$", "");
    }
}
