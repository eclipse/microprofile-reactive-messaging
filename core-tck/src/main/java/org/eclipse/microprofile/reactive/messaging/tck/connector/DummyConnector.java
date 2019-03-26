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
package org.eclipse.microprofile.reactive.messaging.tck.connector;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.MessagingProvider;
import org.eclipse.microprofile.reactive.messaging.connector.IncomingConnectorFactory;
import org.eclipse.microprofile.reactive.messaging.connector.OutgoingConnectorFactory;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.operators.SubscriberBuilder;

import javax.enterprise.context.ApplicationScoped;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ApplicationScoped
public class DummyConnector implements IncomingConnectorFactory, OutgoingConnectorFactory {
    private List<String> elements = new CopyOnWriteArrayList<>();

    @Override public Class<? extends MessagingProvider> type() {
        return Dummy.class;
    }

    public List<String> elements() {
        return elements;
    }

    @Override public SubscriberBuilder<? extends Message, Void> getSubscriberBuilder(Config config) {
        // Would throw a NoSuchElementException if not set.
        config.getValue("attribute", String.class);

        return ReactiveStreams.<Message<String>>builder().map(Message::getPayload).forEach(s -> elements.add(s));
    }

    @Override public PublisherBuilder<? extends Message> getPublisherBuilder(Config config) {
        String[] values = config.getValue("items", String.class).split(",");

        // Would throw a NoSuchElementException if not set.
        config.getValue("attribute", String.class);

        return ReactiveStreams.fromIterable(Arrays.asList(values)).map(Message::of);
    }
}
