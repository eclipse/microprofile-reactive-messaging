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

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.eclipse.microprofile.reactive.messaging.spi.ConnectorFactory;
import org.eclipse.microprofile.reactive.messaging.spi.IncomingConnectorFactory;
import org.eclipse.microprofile.reactive.messaging.spi.OutgoingConnectorFactory;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.operators.SubscriberBuilder;

import javax.inject.Inject;

@Connector(HealthTestConnector.ID)
public class HealthTestConnector implements OutgoingConnectorFactory, IncomingConnectorFactory {

    public static final String ID = "test-connector";

    @Inject
    private ChannelRegister channelRegisterBean;

    @Override
    public PublisherBuilder<? extends Message<?>> getPublisherBuilder(final Config config) {
        return ReactiveStreams.fromPublisher(
            channelRegisterBean.<String>get(config.getValue(ConnectorFactory.CHANNEL_NAME_ATTRIBUTE, String.class))
                .publisher()
        ).map(Message::of);
    }

    @Override
    public SubscriberBuilder<? extends Message<?>, Void> getSubscriberBuilder(final Config config) {
        return ReactiveStreams.<Message<String>>builder()
            .map(Message::getPayload)
            .onError(Throwable::printStackTrace)
            .to(channelRegisterBean.<String>get(config.getValue(ConnectorFactory.CHANNEL_NAME_ATTRIBUTE, String.class))
                .subscriber());
    }
}
