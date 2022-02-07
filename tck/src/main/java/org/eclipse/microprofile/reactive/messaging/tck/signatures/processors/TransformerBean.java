/*
 * Copyright (c) 2018, 2021 Contributors to the Eclipse Foundation
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
package org.eclipse.microprofile.reactive.messaging.tck.signatures.processors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.reactivestreams.Publisher;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TransformerBean {

    private Map<String, List<String>> collector = new ConcurrentHashMap<>();

    private static final List<String> EXPECTED = Arrays.asList(
            "1", "1",
            "2", "2",
            "3", "3",
            "4", "4",
            "5", "5",
            "6", "6",
            "7", "7",
            "8", "8",
            "9", "9",
            "10", "10");

    private static Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    private static void increment(String counter) {
        counters.computeIfAbsent(counter, x -> new AtomicInteger(0)).incrementAndGet();
    }

    @Outgoing("publisher-for-publisher-message")
    public PublisherBuilder<Integer> streamForProcessorOfMessages() {
        return ReactiveStreams.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    @Outgoing("publisher-for-publisher-payload")
    public PublisherBuilder<Integer> streamForProcessorOfPayloads() {
        return ReactiveStreams.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    @Outgoing("publisher-for-publisher-builder-message")
    public PublisherBuilder<Integer> streamForProcessorBuilderOfMessages() {
        return ReactiveStreams.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    @Outgoing("publisher-for-publisher-builder-payload")
    public PublisherBuilder<Integer> streamForProcessorBuilderOfPayloads() {
        return ReactiveStreams.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    @Incoming("publisher-message")
    public void getMessgesFromProcessorOfMessages(String value) {
        add("message", value);
    }

    @Incoming("publisher-payload")
    public void getMessgesFromProcessorOfPayloads(String value) {
        add("payload", value);
    }

    @Incoming("publisher-builder-message")
    public void getMessgesFromProcessorBuilderOfMessages(String value) {
        add("builder-message", value);
    }

    @Incoming("publisher-builder-payload")
    public void getMessgesFromProcessorBuilderOfPayloads(String value) {
        add("builder-payload", value);
    }

    @Incoming("publisher-for-publisher-message")
    @Outgoing("publisher-message")
    public Publisher<Message<String>> processorOfMessages(Publisher<Message<Integer>> stream) {
        increment("publisher-message");
        return ReactiveStreams.fromPublisher(stream)
                .map(Message::getPayload)
                .map(i -> i + 1)
                .flatMap(i -> ReactiveStreams.of(i, i))
                .map(i -> Integer.toString(i))
                .map(Message::of)
                .buildRs();
    }

    @Incoming("publisher-for-publisher-payload")
    @Outgoing("publisher-payload")
    public Publisher<String> processorOfPayloads(Publisher<Integer> stream) {
        increment("publisher-payload");
        return ReactiveStreams.fromPublisher(stream)
                .map(i -> i + 1)
                .flatMap(i -> ReactiveStreams.of(i, i))
                .map(i -> Integer.toString(i))
                .buildRs();
    }

    @Incoming("publisher-for-publisher-builder-message")
    @Outgoing("publisher-builder-message")
    public PublisherBuilder<Message<String>> processorBuilderOfMessages(PublisherBuilder<Message<Integer>> stream) {
        increment("publisher-builder-message");
        return stream
                .map(Message::getPayload)
                .map(i -> i + 1)
                .flatMap(i -> ReactiveStreams.of(i, i))
                .map(i -> Integer.toString(i))
                .map(Message::of);
    }

    @Incoming("publisher-for-publisher-builder-payload")
    @Outgoing("publisher-builder-payload")
    public PublisherBuilder<String> processorBuilderOfPayloads(PublisherBuilder<Integer> stream) {
        increment("publisher-builder-payload");
        return stream
                .map(i -> i + 1)
                .flatMap(i -> ReactiveStreams.of(i, i))
                .map(i -> Integer.toString(i));
    }

    private void add(String key, String value) {
        collector.computeIfAbsent(key, x -> new CopyOnWriteArrayList<>()).add(value);
    }

    void verify() {
        await().until(() -> collector.size() == 4);
        assertThat(collector).hasSize(4).allSatisfy((k, v) -> assertThat(v).containsExactlyElementsOf(EXPECTED));
        assertThat(counters.get("publisher-message")).hasValue(1);
        assertThat(counters.get("publisher-payload")).hasValue(1);
        assertThat(counters.get("publisher-builder-message")).hasValue(1);
        assertThat(counters.get("publisher-builder-payload")).hasValue(1);
    }

}
