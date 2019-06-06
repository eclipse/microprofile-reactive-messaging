/**
 * Copyright (c) 2018-2019 Contributors to the Eclipse Foundation
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
package org.eclipse.microprofile.reactive.messaging.tck.signatures.publishers;

import io.reactivex.Flowable;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.reactivestreams.Publisher;

import javax.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.eclipse.microprofile.reactive.messaging.tck.TckBase.EXECUTOR;

@ApplicationScoped
public class PublisherBean {

    private static final Map<String, AtomicInteger> COUNTERS = new ConcurrentHashMap<>();

    private static void increment(String counter) {
        COUNTERS.computeIfAbsent(counter, x -> new AtomicInteger(0)).incrementAndGet();
    }

    static Map<String, AtomicInteger> getCounters() {
        return COUNTERS;
    }

    @Outgoing("publisher-message")
    public Publisher<Message<String>> getAPublisherProducingMessage() {
        increment("publisher-message");
        return ReactiveStreams.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
            .flatMap(i -> ReactiveStreams.of(i, i))
            .map(i -> Integer.toString(i))
            .map(Message::of)
            .buildRs();
    }

    @Outgoing("publisher-payload")
    public Publisher<String> getAPublisherProducingPayload() {
        increment("publisher-payload");
        return ReactiveStreams.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
            .flatMap(i -> ReactiveStreams.of(i, i))
            .map(i -> Integer.toString(i))
            .buildRs();
    }

    @Outgoing("publisher-builder-message")
    public PublisherBuilder<Message<String>> getAPublisherBuilderProducingMessage() {
        increment("publisher-builder-message");
        return ReactiveStreams.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
            .flatMap(i -> ReactiveStreams.of(i, i))
            .map(i -> Integer.toString(i))
            .map(Message::of);
    }

    @Outgoing("publisher-builder-payload")
    public PublisherBuilder<String> getAPublisherBuilderProducingPayload() {
        increment("publisher-builder-payload");
        return ReactiveStreams.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
            .flatMap(i -> ReactiveStreams.of(i, i))
            .map(i -> Integer.toString(i));
    }

    @Outgoing("publisher-flowable-message")
    public Flowable<Message<String>> getASubclassOfPublisherProducingMessage() {
        increment("publisher-flowable-message");
        return getASubclassOfPublisherProducingPayload().map(Message::of);
    }

    @Outgoing("publisher-flowable-payload")
    public Flowable<String> getASubclassOfPublisherProducingPayload() {
        increment("publisher-flowable-payload");
        return Flowable.range(1, 10).flatMap(i -> Flowable.just(i, i)).map(i -> Integer.toString(i));
    }

    private AtomicInteger generatorPayload = new AtomicInteger();
    private AtomicInteger generatorMessage = new AtomicInteger();
    private AtomicInteger generatorAsyncMessage = new AtomicInteger();
    private AtomicInteger generatorAsyncPayload = new AtomicInteger();

    @Outgoing("generator-payload")
    public int getPayloads() {
        increment("generator-payload");
        return generatorPayload.incrementAndGet();
    }

    @Outgoing("generator-message")
    public Message<Integer> getMessage() {
        increment("generator-message");
        return Message.of(generatorMessage.incrementAndGet());
    }


    @Outgoing("generator-message-async")
    public CompletionStage<Message<Integer>> getMessageAsync() {
        increment("generator-message-async");
        return CompletableFuture.supplyAsync(() -> Message.of(generatorAsyncMessage.incrementAndGet()), EXECUTOR);
    }

    @Outgoing("generator-payload-async")
    public CompletionStage<Integer> getPayloadAsync() {
        increment("generator-payload-async");
        return CompletableFuture.supplyAsync(() -> generatorAsyncPayload.incrementAndGet(), EXECUTOR);
    }

}
