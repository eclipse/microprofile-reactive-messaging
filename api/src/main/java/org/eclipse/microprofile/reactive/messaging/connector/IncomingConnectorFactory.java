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
package org.eclipse.microprofile.reactive.messaging.connector;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.MessagingProvider;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;

import java.util.NoSuchElementException;

/**
 * SPI used to implement a connector managing a source of messages for a specific <em>transport</em>. Typically, to
 * handle the consumption of records from Kafka, the reactive messaging extension would need to implement a {@code bean}
 * implementing this interface. This bean is called for every {@code stream} that need to be created for this specific
 * <em>transport</em> (so Kafka in the example). These streams are connected to methods annotated with
 * {@link org.eclipse.microprofile.reactive.messaging.Incoming}.
 * <p>
 * The factory is called to create a {@code stream} for each configured <em>transport</em>. The configuration is done using
 * MicroProfile Config. The following snippet gives an example for a hypothetical Kafka connector:
 *
 * <pre>
 * mp.messaging.incoming.my-stream.type=i.e.m.reactive.messaging.impl.kafka.Kafka
 * mp.messaging.incoming.my-stream.bootstrap-servers=localhost:9092
 * mp.messaging.incoming.my-stream.topic=my-topic
 * ...
 * </pre>
 * <p>
 * This configuration keys are structured as follows: {@code mp.messaging.[incoming|outgoing].stream-name.attribute}.
 * <p>
 * The {@code stream-name} segment in the configuration key corresponds to the name of the stream used in the
 * {@code Incoming} annotation:
 *
 * <pre>
 * &#64;Incoming("my-stream")
 * public void consume(String s) {
 *      // ...
 * }
 * </pre>
 * <p>
 * The set of attributes depend on the connector and transport layer (typically, bootstrap-servers is Kafka specific). The
 * {@code type} attribute is mandatory and indicates the fully qualified name of the {@link MessagingProvider}
 * implementation. It must match the class returned by the {@link #type()} method. This is how a reactive messaging
 * implementation looks for the specific {@link IncomingConnectorFactory} required for a stream. In the previous
 * configuration, the reactive extension implementation would need to find the {@link IncomingConnectorFactory} returning
 * the {@code i.e.m.reactive.messaging.impl.kafka.Kafka} class as result to its {@link #type()} method to create the
 * {@code my-stream} stream. Note that if the connector cannot be found, the deployment must be failed.
 * <p>
 * The {@link #getPublisherBuilder(Config)} is called for every stream that needs to be created. The {@link Config} object
 * passed to the method contains a subset of the global configuration, and with the prefixes removed. So for the previous
 * configuration, it would be:
 * <pre>
 * type =  i.e.m.reactive.messaging.impl.kafka.Kafka
 * bootstrap-services = localhost:9092
 * topic = my-topic
 * </pre>
 * <p>
 * So the connector implementation can retrieves the value with {@link Config#getValue(String, Class)} and
 * {@link Config#getOptionalValue(String, Class)}.
 * <p>
 * If the configuration is invalid, the {@link #getPublisherBuilder(Config)} method must throw an
 * {@link IllegalArgumentException}, caught by the reactive messaging implementation and failing the deployment.
 * <p>
 * Note that Reactive Messaging implementation must support the configuration format described here, implementation may
 * deliver other approaches to create the streams.
 */
public interface IncomingConnectorFactory {

    /**
     * Gets the {@link MessagingProvider} class associated with this {@link IncomingConnectorFactory}.
     * Note that the {@link MessagingProvider} is a user-facing interface used in the configuration.
     *
     * @return the {@link MessagingProvider} associated with this {@link IncomingConnectorFactory}. Must not be
     * {@code null}. Returning {@code null} triggers a deployment failure.
     */
    Class<? extends MessagingProvider> type();

    /**
     * Creates a <em>stream</em> for the given configuration. The given configuration {@code type} attribute matches the
     * {@link #type()}.
     * <p>
     * Note that the connection to the <em>transport</em> or <em>broker</em> is generally postponed until the
     * subscription.
     *
     * @param config the configuration, never {@code null}
     * @return the created {@link PublisherBuilder}, must not be {@code null}.
     * @throws IllegalArgumentException if the configuration is invalid.
     * @throws NoSuchElementException if the configuration does not contain an expected attribute.
     */
    PublisherBuilder<? extends Message> getPublisherBuilder(Config config);

}
