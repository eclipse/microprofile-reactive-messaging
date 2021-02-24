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
public class TestPublisher<PAYLOAD> implements Publisher<PAYLOAD> {

    private final CompletableFuture<Subscriber<? super PAYLOAD>> subscriberFuture = new CompletableFuture<>();
    private final CompletableFuture<Void> cancelFuture = new CompletableFuture<>();
    private final long timeout;
    private final TimeUnit timeUnit;
    private final AtomicReference<State> state = new AtomicReference<>(State.INIT);

    private enum State {
        INIT, READY, FAILED, CANCELLED, COMPLETED
    }


    public TestPublisher(long timeout, TimeUnit timeUnit) {
        this.timeout = timeout;
        this.timeUnit = timeUnit;
    }

    @Override
    public void subscribe(final Subscriber<? super PAYLOAD> subscriber) {
        subscriberFuture.complete(subscriber);
    }

    public TestPublisher<PAYLOAD> ready() {
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

    public TestPublisher<PAYLOAD> emit(PAYLOAD payload) {
        awaitSubscriber().onNext(payload);
        return this;
    }

    public TestPublisher<PAYLOAD> fail(Throwable t) {
        if (state.compareAndSet(State.READY, State.FAILED)) {
            awaitSubscriber().onError(t);
        }
        return this;
    }

    public TestPublisher<PAYLOAD> complete() {
        if (state.compareAndSet(State.READY, State.COMPLETED)) {
            awaitSubscriber().onComplete();
        }
        return this;
    }

    public TestPublisher<PAYLOAD> awaitCancel() {
        try {
            cancelFuture.get(timeout, timeUnit);
            return this;
        }
        catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private Subscriber<? super PAYLOAD> awaitSubscriber() {
        try {
            return subscriberFuture.get(timeout, timeUnit);
        }
        catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
