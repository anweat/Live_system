package common.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import javax.validation.constraints.Pattern;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户基础信息DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO extends BaseDTO {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 昵称 */
    @NotBlank(message = "昵称不能为空")
    private String nickname;

    /** 邮箱 */
    @NotBlank(message = "邮箱不能为空")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@(.+)$", message = "邮箱格式不正确")
    private String email;

    /** 电话号码 */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "电话号码格式不正确")
    private String phoneNumber;

    /** 性别: UNKNOWN(0), MALE(1), FEMALE(2) */
    @Min(value = 0)
    @Max(value = 2)
    private Integer gender;

    /** 头像URL */
    private String avatarUrl;

    /** 个人简介 */
    private String bio;

    /** 验证方式：NEW_USER为注册新用户，UPDATE为更新用户信息 */
    private String operationType;

    /**
     * 验证用户DTO的有效性
     */
    public boolean validate() {
        if (username == null || username.trim().isEmpty() || username.length() > 64) {
            return false;
        }
        if (nickname == null || nickname.trim().isEmpty() || nickname.length() > 128) {
            return false;
        }
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return false;
        }
        if (gender != null && (gender < 0 || gender > 2)) {
            return false;
        }
        return true;
    }
}
