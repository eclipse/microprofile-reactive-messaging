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
package org.eclipse.microprofile.reactive.messaging.tck.signatures.processors;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.reactivestreams.Publisher;

import javax.enterprise.context.ApplicationScoped;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@ApplicationScoped
public class PublisherBean {

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
        "10", "10"
    );

    @Outgoing("publisher-for-processor-publisher-message")
    public PublisherBuilder<Integer> streamForProcessorOfMessages() {
        return ReactiveStreams.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    @Outgoing("publisher-for-processor-publisher-payload")
    public PublisherBuilder<Integer> streamForProcessorOfPayloads() {
        return ReactiveStreams.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    @Outgoing("publisher-for-processor-publisher-builder-message")
    public PublisherBuilder<Integer> streamForProcessorBuilderOfMessages() {
        return ReactiveStreams.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    @Outgoing("publisher-for-processor-publisher-builder-payload")
    public PublisherBuilder<Integer> streamForProcessorBuilderOfPayloads() {
        return ReactiveStreams.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    @Incoming("processor-publisher-message")
    public void getMessgesFromProcessorOfMessages(String value) {
        add("processor-message", value);
    }

    @Incoming("processor-publisher-payload")
    public void getMessgesFromProcessorOfPayloads(String value) {
        add("processor-payload", value);
    }

    @Incoming("processor-publisher-builder-message")
    public void getMessgesFromProcessorBuilderOfMessages(String value) {
        add("processor-builder-message", value);
    }

    @Incoming("processor-publisher-builder-payload")
    public void getMessgesFromProcessorBuilderOfPayloads(String value) {
        add("processor-builder-payload", value);
    }

    @Incoming("publisher-for-processor-publisher-message")
    @Outgoing("processor-publisher-message")
    public Publisher<Message<String>> processorOfMessages(Message<Integer> message) {
        return ReactiveStreams.of(message)
            .map(Message::getPayload)
            .map(i -> i + 1)
            .flatMap(i -> ReactiveStreams.of(i, i))
            .map(i -> Integer.toString(i))
            .map(Message::of)
            .buildRs();
    }

    @Incoming("publisher-for-processor-publisher-payload")
    @Outgoing("processor-publisher-payload")
    public Publisher<String> processorOfPayloads(int value) {
        return ReactiveStreams.of(value)
            .map(i -> i + 1)
            .flatMap(i -> ReactiveStreams.of(i, i))
            .map(i -> Integer.toString(i))
            .buildRs();
    }

    @Incoming("publisher-for-processor-publisher-builder-message")
    @Outgoing("processor-publisher-builder-message")
    public PublisherBuilder<Message<String>> processorBuilderOfMessages(Message<Integer> message) {
        return ReactiveStreams.of(message)
            .map(Message::getPayload)
            .map(i -> i + 1)
            .flatMap(i -> ReactiveStreams.of(i, i))
            .map(i -> Integer.toString(i))
            .map(Message::of);
    }

    @Incoming("publisher-for-processor-publisher-builder-payload")
    @Outgoing("processor-publisher-builder-payload")
    public PublisherBuilder<String> processorBuilderOfPayloads(int value) {
        return ReactiveStreams.of(value)
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
    }

}
