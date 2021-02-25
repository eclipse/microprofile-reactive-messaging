/**
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.microprofile.reactive.messaging.tck.health;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Test subscriber able to block until desired signal from upstream is received.
 *
 * @param <PAYLOAD> payload type
 */
class TestSubscriber<PAYLOAD> implements Subscriber<PAYLOAD> {

    private final CompletableFuture<Subscription> subscription = new CompletableFuture<>();
    private final CompletableFuture<Throwable> error = new CompletableFuture<>();
    private final CompletableFuture<Void> complete = new CompletableFuture<>();
    private final long timeout;
    private final TimeUnit timeoutUnit;

    /**
     * Create new test subscriber with timeout for all blocking operations.
     *
     * @param timeout timeout value
     * @param timeoutUnit unit for evaluation of timeout value
     */
    TestSubscriber(long timeout, TimeUnit timeoutUnit) {
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
    }

    @Override
    public void onSubscribe(final Subscription subscription) {
        this.subscription.complete(subscription);
    }

    @Override
    public void onNext(final PAYLOAD payload) {
        //noop
    }

    @Override
    public void onError(final Throwable t) {
        error.complete(t);
    }

    @Override
    public void onComplete() {
        complete.complete(null);
    }

    /**
     * Block until onError signal is received from upstream.
     *
     * @return cause of the error signal
     */
    public Throwable awaitError() {
        try {
            return this.error.get(timeout, timeoutUnit);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Block until onComplete signal is received from upstream.
     */
    public void awaitCompletion() {
        try {
            this.complete.get(timeout, timeoutUnit);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Block until onSubscribe signal is received from upstream.
     *
     * @return this
     */
    public ReadyManualSubscriber<PAYLOAD> awaitSubscription() {
        this.getSubscription();
        return new ReadyManualSubscriber<PAYLOAD>(this);
    }

    private Subscription getSubscription() {
        try {
            return this.subscription.get(timeout, timeoutUnit);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Wrapped test subscriber with capability of sending cancel signal.
     *
     * @param <PAYLOAD> payload type
     */
    static class ReadyManualSubscriber<PAYLOAD> {

        private final TestSubscriber<PAYLOAD> testSubscriber;

        ReadyManualSubscriber(final TestSubscriber<PAYLOAD> testSubscriber) {
            this.testSubscriber = testSubscriber;
        }

        /**
         * Block until onSubscribe signal is received from upstream,
         * then send cancel signal to upstream.
         *
         * @return this ready subscriber
         */
        public ReadyManualSubscriber<PAYLOAD> cancel() {
            this.testSubscriber.getSubscription().cancel();
            return this;
        }
    }
}
