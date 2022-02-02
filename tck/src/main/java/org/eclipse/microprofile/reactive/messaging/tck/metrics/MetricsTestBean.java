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
package org.eclipse.microprofile.reactive.messaging.tck.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment.Strategy;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MetricsTestBean {

    public static final String CONNECTOR_IN = "channel-connector-in";
    public static final String CONNECTOR_PROCESS = "channel-connector-process";
    public static final String CONNECTOR_OUT = "channel-connector-out";

    public static final String CHANNEL_APP_A = "channel-app-a";
    public static final String CHANNEL_APP_B = "channel-app-b";

    private AtomicInteger inAppMessagesReceived = new AtomicInteger(0);

    @Incoming(CONNECTOR_IN)
    @Outgoing(CONNECTOR_PROCESS)
    public String simpleMapping(String a) {
        return a + "-test";
    }

    @Incoming(CONNECTOR_PROCESS)
    @Outgoing(CONNECTOR_OUT)
    @Acknowledgment(Strategy.PRE_PROCESSING)
    public PublisherBuilder<Message<String>> split(Message<String> a) {
        List<Message<String>> messages = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            messages.add(Message.of(a.getPayload() + "-" + i));
        }
        return ReactiveStreams.fromIterable(messages);
    }

    @Outgoing(CHANNEL_APP_A)
    public PublisherBuilder<String> produce() {
        return ReactiveStreams.of("test-a", "test-b", "test-c");
    }

    @Incoming(CHANNEL_APP_A)
    @Outgoing(CHANNEL_APP_B)
    public PublisherBuilder<String> split(String input) {
        List<String> messages = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            messages.add(input + "-" + i);
        }
        return ReactiveStreams.fromIterable(messages);
    }

    @Incoming(CHANNEL_APP_B)
    public void receive(String input) {
        inAppMessagesReceived.incrementAndGet();
    }

    public int getInAppMessagesReceived() {
        return inAppMessagesReceived.get();
    }
}
