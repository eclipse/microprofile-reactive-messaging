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
import java.util.concurrent.atomic.AtomicReference;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Simple manual publisher with guarded state, not thread safe, sequential non-concurrent calls are expected.
 * @param <PAYLOAD> payload type
 */
class TestPublisher<PAYLOAD> implements Publisher<PAYLOAD> {

    private final CompletableFuture<Subscriber<? super PAYLOAD>> subscriberFuture = new CompletableFuture<>();
    private final CompletableFuture<Void> cancelFuture = new CompletableFuture<>();
    private final long timeout;
    private final TimeUnit timeUnit;
    private final AtomicReference<State> state = new AtomicReference<>(State.INIT);

    private enum State {
        /**
         * Initial state until {@link TestPublisher#ready()} is called.
         */
        INIT,
        /**
         * After {@link TestPublisher#ready()} is called until {@link TestPublisher#fail(Throwable)} or
         * {@link TestPublisher#complete()} is invoked or cancel signal is received from downstream.
         */
        READY,
        /**
         * After {@link TestPublisher#fail(Throwable)} is invoked.
         */
        FAILED,
        /**
         * After cancel signal is received from downstream.
         */
        CANCELLED,
        /**
         * After {@link TestPublisher#complete()} is invoked.
         */
        COMPLETED
    }

    /**
     * Create new test publisher with timeout for all blocking operations.
     *
     * @param timeout timeout value
     * @param timeUnit unit for evaluation of timeout value
     */
    TestPublisher(long timeout, TimeUnit timeUnit) {
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    @Override
    public void subscribe(final Subscriber<? super PAYLOAD> subscriber) {
        subscriberFuture.complete(subscriber);
    }

    /**
     * Block until subscriber is available
     * and send onSubscribe signal to downstream if publisher is in {@link State#INIT INIT} state, or do nothing.
     *
     * @return this test publisher
     */
    TestPublisher<PAYLOAD> ready() {
        if (state.compareAndSet(State.INIT, State.READY)) {
            awaitSubscriber().onSubscribe(new Subscription() {
                @Override
                public void request(final long n) {
                    //noop
                }

                @Override
                public void cancel() {
                    if (state.getAndUpdate(s -> s != State.COMPLETED && s != State.FAILED ? State.CANCELLED : s)
                        != State.CANCELLED) {
                        cancelFuture.complete(null);
                    }
                }
            });
        }
        return this;
    }

    /**
     * Block until subscriber is available
     * and send onError signal to downstream if publisher is in {@link State#READY READY} state, or do nothing.
     *
     * @return this test publisher
     */
    TestPublisher<PAYLOAD> fail(Throwable t) {
        if (state.compareAndSet(State.READY, State.FAILED)) {
            awaitSubscriber().onError(t);
        }
        return this;
    }

    /**
     * Block until subscriber is available
     * and send onComplete signal to downstream if publisher is in {@link State#READY READY} state, or do nothing.
     *
     * @return this test publisher
     */
    TestPublisher<PAYLOAD> complete() {
        if (state.compareAndSet(State.READY, State.COMPLETED)) {
            awaitSubscriber().onComplete();
        }
        return this;
    }

    /**
     * Block until cancel signal is received from the subscriber.
     *
     * @return this test publisher
     */
    TestPublisher<PAYLOAD> awaitCancel() {
        try {
            cancelFuture.get(timeout, timeUnit);
            return this;
        }
        catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Wait for a subscriber,
     * block till {@link TestPublisher#subscribe(org.reactivestreams.Subscriber) subscribe} method is invoked.
     *
     * @return subscriber of this publisher
     */
    private Subscriber<? super PAYLOAD> awaitSubscriber() {
        try {
            return subscriberFuture.get(timeout, timeUnit);
        }
        catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
