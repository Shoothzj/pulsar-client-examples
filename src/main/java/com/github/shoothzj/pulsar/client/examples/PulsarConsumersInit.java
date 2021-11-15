package com.github.shoothzj.pulsar.client.examples;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Consumer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author hezhangjian
 */
@Slf4j
public class PulsarConsumersInit {

    private final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("pulsar-consumers-init").build();

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1, threadFactory);

    private final Map<String, Consumer<byte[]>> consumerMap = new ConcurrentHashMap<>();

    private int initIndex = 0;

    private final List<String> topics;

    public PulsarConsumersInit(List<String> topics) {
        this.topics = topics;
    }

    public void init() {
        executorService.scheduleWithFixedDelay(this::initWithRetry, 0, 10, TimeUnit.SECONDS);
    }

    private void initWithRetry() {
        if (initIndex == topics.size()) {
            executorService.shutdown();
            return;
        }
        for (; initIndex < topics.size(); initIndex++) {
            try {
                final PulsarClientInit instance = PulsarClientInit.getInstance();
                final Consumer<byte[]> consumer = instance.getPulsarClient().newConsumer().topic(topics.get(initIndex)).messageListener(new DummyMessageListener<>()).subscribe();
                consumerMap.put(topics.get(initIndex), consumer);
            } catch (Exception e) {
                log.error("init pulsar producer error, exception is ", e);
                break;
            }
        }
    }

    public Consumer<byte[]> getConsumer(String topic) {
        return consumerMap.get(topic);
    }
}
