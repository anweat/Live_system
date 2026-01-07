package common.bean.liveroom;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import common.bean.user.Audience;
import common.bean.liveroom.LiveRoom;

/**
 * 直播会话观众 - 记录观众在某场直播中的信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "live_session_audience")
public class LiveSessionAudience implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_audience_id")
    private Long id;

    /** 直播间 */
    @ManyToOne
    @JoinColumn(name = "live_room_id")
    private LiveRoom liveRoom;

    /** 观众 */
    @ManyToOne
    @JoinColumn(name = "audience_id")
    private Audience audience;

    /** 进入时间 */
    private LocalDateTime joinTime;

    /** 离开时间 */
    private LocalDateTime leaveTime;

    /** 观看时长 (秒) */
    private Long watchDuration;
}