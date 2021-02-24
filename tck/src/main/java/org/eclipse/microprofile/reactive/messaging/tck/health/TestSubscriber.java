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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class TestSubscriber<PAYLOAD> implements Subscriber<PAYLOAD> {

    private CompletableFuture<Subscription> subscription = new CompletableFuture<>();
    private CompletableFuture<Throwable> error = new CompletableFuture<>();
    private CompletableFuture<Void> complete = new CompletableFuture<>();
    private List<PAYLOAD> receivedItems = new CopyOnWriteArrayList<>();
    private long timeout;
    private TimeUnit timeoutUnit;

    public TestSubscriber(long timeout, TimeUnit timeoutUnit) {
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
    }

    @Override
    public void onSubscribe(final Subscription subscription) {
        this.subscription.complete(subscription);
    }

    @Override
    public void onNext(final PAYLOAD payload) {
        receivedItems.add(payload);
    }

    @Override
    public void onError(final Throwable t) {
        error.complete(t);
    }

    @Override
    public void onComplete() {
        complete.complete(null);
    }

    private Subscription getSubscription() {
        try {
            return this.subscription.get(timeout, timeoutUnit);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public Throwable awaitError() {
        try {
            return this.error.get(timeout, timeoutUnit);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public void awaitCompletion() {
        try {
            this.complete.get(timeout, timeoutUnit);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public ReadyManualSubscriber<PAYLOAD> awaitSubscription() {
        this.getSubscription();
        return new ReadyManualSubscriber<PAYLOAD>(this);
    }

    public static class ReadyManualSubscriber<PAYLOAD> {

        private final TestSubscriber<PAYLOAD> testSubscriber;

        public ReadyManualSubscriber(final TestSubscriber<PAYLOAD> testSubscriber) {
            this.testSubscriber = testSubscriber;
        }

        public ReadyManualSubscriber<PAYLOAD> request(long n) {
            this.testSubscriber.getSubscription().request(n);
            return this;
        }

        public ReadyManualSubscriber<PAYLOAD> cancel() {
            this.testSubscriber.getSubscription().cancel();
            return this;
        }
    }
}
