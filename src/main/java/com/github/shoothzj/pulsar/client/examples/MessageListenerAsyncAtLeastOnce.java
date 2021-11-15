package com.github.shoothzj.pulsar.client.examples;

import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageListener;

/**
 * @author hezhangjian
 */
@Slf4j
public class MessageListenerAsyncAtLeastOnce<T> implements MessageListener<T> {

    @Override
    public void received(Consumer<T> consumer, Message<T> msg) {
        try {
            asyncPayload(msg.getData(), e -> {
                if (e == null) {
                    consumer.acknowledgeAsync(msg);
                } else {
                    log.error("exception is ", e);
                    consumer.negativeAcknowledge(msg);
                }
            });
        } catch (Exception e) {
            // 业务方法可能会抛出异常
            consumer.negativeAcknowledge(msg);
        }
    }

    /**
     * 模拟异步执行的业务方法
     * @param msg 消息体
     * @param sendCallback 异步函数的callback
     */
    private void asyncPayload(byte[] msg, SendCallback sendCallback) {
        if (System.currentTimeMillis() % 2 == 0) {
            sendCallback.callback(null);
        } else {
            sendCallback.callback(new Exception("exception"));
        }
    }

}
