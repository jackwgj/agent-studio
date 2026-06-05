package com.openjiuwen.studio.agent.space.common.feignclient;

import feign.RetryableException;
import feign.Retryer;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class FeignRetry implements Retryer, Cloneable {
    private static final long FIRST_SLEEP_INTERVAL = 50L;

    private static final long SECOND_SLEEP_INTERVAL = 100L;

    private static final long THIRD_SLEEP_INTERVAL = 200L;

    private static final List<Long> SLEEP_INTERVAL_LIST = Arrays.asList(FIRST_SLEEP_INTERVAL, SECOND_SLEEP_INTERVAL, THIRD_SLEEP_INTERVAL);

    private final int maxAttempts;

    int attempt;

    public FeignRetry(int maxAttempts) {
        this.maxAttempts = maxAttempts;
        this.attempt = 1;
    }

    public void continueOrPropagate(RetryableException e) {
        if (this.attempt > this.maxAttempts) {
            throw e;
        } else {
            try {
                long interval = SLEEP_INTERVAL_LIST.get(attempt - 1);
                log.info("retry https request for the [{}] time.", attempt);
                Thread.sleep(interval);
                this.attempt += 1;
            } catch (InterruptedException var5) {
                log.error("try retry failed, for sleep error, {}", e.getMessage());
                throw e;
            }
        }
    }

    public FeignRetry clone() {
        FeignRetry retry = new FeignRetry(3);
        try {
            retry = (FeignRetry) super.clone();
        } catch (CloneNotSupportedException e) {
            log.error("retryer clone occur error, for msg = {}", e.getMessage());
        }
        return retry;
    }
}
