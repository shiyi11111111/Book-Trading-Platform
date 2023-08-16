package com.shiyi.gulimall.ware.config;

import com.shiyi.gulimall.ware.event.RabbitApplicationEvent;
import com.shiyi.gulimall.ware.event.RetryStoreApplicationEvent;
import com.shiyi.gulimall.ware.rabbitmq.RabbitUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Configuration
public class ApplicationEventConfig {

    @Autowired
    private RabbitUtil rabbitUtil;

    @Autowired
    @Qualifier(value = "threadPoolRetryTaskExecutor")
    private ThreadPoolTaskExecutor threadPoolRetryTaskExecutor;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApplicationEvent(RabbitApplicationEvent<?> event){
        rabbitUtil.sendMsg(event.getData(),event.getMessageId(),event.getExchangeName(),event.getRoutingKey());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApplicationEvent(RetryStoreApplicationEvent<?> event){

        threadPoolRetryTaskExecutor.execute( new Thread(() -> {
            rabbitUtil.retryStorageLimitTimes(event.getData(),event.getRabbitApplicationEvent());
        }));
    }
}
