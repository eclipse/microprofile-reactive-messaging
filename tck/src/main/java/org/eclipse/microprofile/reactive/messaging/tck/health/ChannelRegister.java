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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.enterprise.context.ApplicationScoped;

import org.junit.Assert;

@ApplicationScoped
public class ChannelRegister {

    private final Map<String, Channel<?>> channelMap = new HashMap<>();
    private final ReentrantLock mapLock = new ReentrantLock();

    @SuppressWarnings("unchecked")
    <P> Channel<P> get(String channelName) {
        try {
            mapLock.lock();
            channelMap.putIfAbsent(channelName, new Channel<P>());
            return (Channel<P>) channelMap.get(channelName);
        }
        finally {
            mapLock.unlock();
        }
    }

    Collection<Channel<?>> getAllChannels() {
        try {
            mapLock.lock();
            return Collections.unmodifiableCollection(channelMap.values());
        }
        finally {
            mapLock.unlock();
        }
    }

    /**
     * Iterate over all registered channels, on each's publisher invoke onSubscribe signal
     * and block until any onSubscribe signal is received on its subscriber.
     */
    void readyAll() {
        getAllChannels().forEach(Channel::ready);
    }

    /**
     * Iterate over all registered channels, on each's publisher invoke onError signal
     * and block until any error signal is received on its subscriber.
     */
    void failAll() {
        getAllChannels().forEach(Channel::fail);
    }

    /**
     * Iterate over all registered channels, on each's publisher invoke onComplete signal
     * and block until any complete signal is received on its subscriber.
     */
    void completeAll() {
        getAllChannels().forEach(Channel::complete);
    }

    /**
     * Iterate over all registered channels, on each's subscriber subscription invoke cancel signal
     * and block until any cancel signal is received on its publisher.
     */
    void cancelAll() {
        getAllChannels().forEach(Channel::cancel);
    }

    public static class Channel<PAYLOAD> {
        private final TestPublisher<PAYLOAD> publisher;
        private final TestSubscriber<PAYLOAD> subscriber;

        public Channel() {
            publisher = new TestPublisher<>(100, TimeUnit.MILLISECONDS);
            subscriber = new TestSubscriber<>(100, TimeUnit.MILLISECONDS);
        }

        /**
         * Access this channel's publisher.
         *
         * @return this channel's test publisher
         */
        public TestPublisher<PAYLOAD> publisher() {
            return publisher;
        }

        /**
         * Access this channel's subscriber.
         *
         * @return this channel's test subscriber
         */
        public TestSubscriber<PAYLOAD> subscriber() {
            return subscriber;
        }

        /**
         * Trigger onSubscribe signal and block until received by subscriber.
         *
         * @return this channel
         */
        public Channel<PAYLOAD> ready() {
            this.publisher().ready();
            this.subscriber().awaitSubscription();
            return this;
        }

        /**
         * Trigger onComplete signal and block until received by subscriber.
         *
         * @return this channel
         */
        public Channel<PAYLOAD> complete() {
            this.publisher().complete();
            this.subscriber().awaitCompletion();
            return this;
        }

        /**
         * Trigger onError signal and block until received by subscriber.
         *
         * @return this channel
         */
        public Channel<PAYLOAD> fail() {
            Exception exception = new Exception("BOOM!!!");
            this.publisher().fail(exception);
            Assert.assertEquals(exception.getMessage(), this.subscriber().awaitError().getMessage());
            return this;
        }

        /**
         * Trigger cancel signal and block until received by publisher.
         *
         * @return this channel
         */
        public Channel<PAYLOAD> cancel() {
            this.subscriber().awaitSubscription().cancel();
            this.publisher().awaitCancel();
            return this;
        }
    }
}
