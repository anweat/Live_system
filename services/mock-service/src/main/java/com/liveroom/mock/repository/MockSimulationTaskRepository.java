package com.liveroom.mock.repository;

import com.liveroom.mock.entity.MockSimulationTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 模拟任务 Repository
 */
@Repository
public interface MockSimulationTaskRepository extends JpaRepository<MockSimulationTask, Long> {

    /**
     * 根据任务ID查询
     */
    Optional<MockSimulationTask> findByTaskId(String taskId);

    /**
     * 根据直播间ID查询
     */
    List<MockSimulationTask> findByLiveRoomId(Long liveRoomId);

    /**
     * 根据状态查询
     */
    List<MockSimulationTask> findByStatus(String status);

    /**
     * 根据直播间ID和状态查询
     */
    List<MockSimulationTask> findByLiveRoomIdAndStatus(Long liveRoomId, String status);
}
