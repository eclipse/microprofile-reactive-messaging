/**
 * Copyright (c) 2018, 2019 Contributors to the Eclipse Foundation
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

import javax.enterprise.context.ApplicationScoped;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.eclipse.microprofile.reactive.messaging.tck.TckBase.EXECUTOR;

@ApplicationScoped
public class DirectProcessorBean {

  private Map<String, List<String>> collector = new ConcurrentHashMap<>();

  private static final List<String> EXPECTED = Arrays.asList(
    "1",
    "2",
    "3",
    "4",
    "5",
    "6",
    "7",
    "8",
    "9",
    "10"
  );

  private static Map<String, AtomicInteger> counters = new ConcurrentHashMap<>();

  private static void increment(String counter) {
    counters.computeIfAbsent(counter, x -> new AtomicInteger(0)).incrementAndGet();
  }

  @Outgoing("publisher-synchronous-message")
  public PublisherBuilder<Integer> streamForProcessorOfMessages() {
    return ReactiveStreams.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
  }

  @Outgoing("publisher-synchronous-payload")
  public PublisherBuilder<Integer> streamForProcessorOfPayloads() {
    return ReactiveStreams.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
  }

  @Outgoing("publisher-asynchronous-message")
  public PublisherBuilder<Integer> streamForProcessorBuilderOfMessages() {
    return ReactiveStreams.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
  }

  @Outgoing("publisher-asynchronous-payload")
  public PublisherBuilder<Integer> streamForProcessorBuilderOfPayloads() {
    return ReactiveStreams.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
  }

  @Incoming("synchronous-message")
  public void getMessgesFromProcessorOfMessages(String value) {
    add("processor-message", value);
  }

  @Incoming("synchronous-payload")
  public void getMessgesFromProcessorOfPayloads(String value) {
    add("processor-payload", value);
  }

  @Incoming("asynchronous-message")
  public void getMessgesFromProcessorBuilderOfMessages(String value) {
    add("processor-builder-message", value);
  }

  @Incoming("asynchronous-payload")
  public void getMessgesFromProcessorBuilderOfPayloads(String value) {
    add("processor-builder-payload", value);
  }

  @Incoming("publisher-synchronous-message")
  @Outgoing("synchronous-message")
  public Message<String> messageSynchronous(Message<Integer> message) {
    increment("synchronous-message");
    return Message.of(Integer.toString(message.getPayload() + 1));
  }

  @Incoming("publisher-synchronous-payload")
  @Outgoing("synchronous-payload")
  public String payloadSynchronous(int value) {
    increment("synchronous-payload");
    return Integer.toString(value + 1);
  }

  @Incoming("publisher-asynchronous-message")
  @Outgoing("asynchronous-message")
  public CompletionStage<Message<String>> messageAsynchronous(Message<Integer> message) {
    increment("asynchronous-message");
    return CompletableFuture.supplyAsync(() -> Message.of(Integer.toString(message.getPayload() + 1)), EXECUTOR);
  }

  @Incoming("publisher-asynchronous-payload")
  @Outgoing("asynchronous-payload")
  public CompletionStage<String> payloadAsynchronous(int value) {
    increment("asynchronous-payload");
    return CompletableFuture.supplyAsync(() -> Integer.toString(value + 1), EXECUTOR);
  }

  private void add(String key, String value) {
    collector.computeIfAbsent(key, x -> new CopyOnWriteArrayList<>()).add(value);
  }

  void verify() {
    await().until(() -> collector.size() == 4);
    assertThat(collector).hasSize(4).allSatisfy((k, v) -> assertThat(v).containsExactlyElementsOf(EXPECTED));
    assertThat(counters.get("synchronous-message")).hasValue(EXPECTED.size());
    assertThat(counters.get("synchronous-payload")).hasValue(EXPECTED.size());
    assertThat(counters.get("asynchronous-message")).hasValue(EXPECTED.size());
    assertThat(counters.get("asynchronous-payload")).hasValue(EXPECTED.size());
  }

}
