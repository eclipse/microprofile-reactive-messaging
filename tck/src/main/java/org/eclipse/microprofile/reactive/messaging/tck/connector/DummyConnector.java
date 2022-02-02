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
package org.eclipse.microprofile.reactive.messaging.tck.connector;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.eclipse.microprofile.reactive.messaging.spi.IncomingConnectorFactory;
import org.eclipse.microprofile.reactive.messaging.spi.OutgoingConnectorFactory;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.operators.SubscriberBuilder;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Connector("Dummy")
public class DummyConnector implements IncomingConnectorFactory, OutgoingConnectorFactory {
    private List<String> elements = new CopyOnWriteArrayList<>();

    /*
     * Stores the received configs.
     */
    private List<Config> configs = new CopyOnWriteArrayList<>();

    List<String> elements() {
        return elements;
    }

    List<Config> getReceivedConfigurations() {
        return configs;
    }

    @Override
    public SubscriberBuilder<? extends Message<?>, Void> getSubscriberBuilder(Config config) {
        // Check mandatory attributes
        assertThat(config.getValue(CHANNEL_NAME_ATTRIBUTE, String.class)).isNotBlank();
        assertThat(config.getValue(CONNECTOR_ATTRIBUTE, String.class)).isEqualTo("Dummy");

        configs.add(config);

        // Would throw a NoSuchElementException if not set.
        config.getValue("attribute", String.class);
        config.getValue("common-A", String.class);
        config.getValue("common-B", String.class);

        return ReactiveStreams.<Message<String>>builder().map(Message::getPayload).forEach(s -> elements.add(s));
    }

    @Override
    public PublisherBuilder<? extends Message<?>> getPublisherBuilder(Config config) {
        configs.add(config);
        String[] values = config.getValue("items", String.class).split(",");

        // Check mandatory attributes
        assertThat(config.getValue(CHANNEL_NAME_ATTRIBUTE, String.class)).isNotBlank();
        assertThat(config.getValue(CONNECTOR_ATTRIBUTE, String.class)).isEqualTo("Dummy");

        // Would throw a NoSuchElementException if not set.
        config.getValue("attribute", String.class);
        config.getValue("common-A", String.class);
        config.getValue("common-B", String.class);

        return ReactiveStreams.fromIterable(Arrays.asList(values)).map(Message::of);
    }

}
