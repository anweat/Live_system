package common.bean.liveroom;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import common.bean.liveroom.LiveRoom;
import common.bean.user.Audience;

/**
 * 弹幕消息实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "message")
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    /** 所属直播间 */
    @ManyToOne
    @JoinColumn(name = "live_room_id")
    private LiveRoom liveRoom;

    /** 发送者 */
    @ManyToOne
    @JoinColumn(name = "sender_id")
    private Audience sender;

    /** 消息内容 */
    private String content;

    /** 创建时间 */
    private LocalDateTime createTime;
}