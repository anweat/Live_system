package com.liveroom.mock.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Mock服务配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "mock")
public class MockProperties {

    private BotConfig bot = new BotConfig();
    private SimulationConfig simulation = new SimulationConfig();
    private RandomConfig random = new RandomConfig();

    @Data
    public static class BotConfig {
        private String namePrefix = "Bot_";
        private int defaultBatchSize = 50;
        private int maxBatchSize = 500;
    }

    @Data
    public static class SimulationConfig {
        private boolean enabled = true;
        private long enterIntervalMin = 1000;
        private long enterIntervalMax = 5000;
        private long messageIntervalMin = 3000;
        private long messageIntervalMax = 10000;
        private long rechargeIntervalMin = 10000;
        private long rechargeIntervalMax = 30000;
        private long leaveIntervalMin = 30000;
        private long leaveIntervalMax = 120000;
    }

    @Data
    public static class RandomConfig {
        private int genderMaleRate = 55;
        private int consumptionLowRate = 60;
        private int consumptionMediumRate = 30;
        private int consumptionHighRate = 10;
        private double rechargeMin = 1.0;
        private double rechargeMax = 1000.0;
        private int tagCountMin = 1;
        private int tagCountMax = 5;
    }
}
