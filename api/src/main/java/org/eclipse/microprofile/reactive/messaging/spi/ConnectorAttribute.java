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
package org.eclipse.microprofile.reactive.messaging.spi;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Connector implementor can use this annotation to document attributes supported by their connector. This allows tools
 * (IDE, annotation processors...) to extract that data to provide code completion or documentation generation.
 *
 * Each attribute is represented by an instance of {@code ConnectorAttribute}. For example:
 *
 * <pre>
 * {@code
 *  &#64;ConnectorAttribute(name = "bootstrap.servers", alias = "kafka.bootstrap.servers", type = "string",
 *      defaultValue = "localhost:9092", direction = Direction.INCOMING_AND_OUTGOING,
 *      description = "...")
 *  &#64;ConnectorAttribute(name = "topic", type = "string", direction = Direction.INCOMING_AND_OUTGOING,
 *      description = "...")
 *  &#64;ConnectorAttribute(name = "value-deserialization-failure-handler", type = "string", direction = Direction.INCOMING,
 *      description = "...")
 *  &#64;ConnectorAttribute(name = "merge", direction = OUTGOING, type = "boolean", defaultValue = "false",
 *      description = "...")
 *  &#64;Connector("my-connector")
 *  public class MyConnector implements  IncomingConnectorFactory, OutgoingConnectorFactory {
 *    ...
 * }
 * </pre>
 *
 */
@Retention(RetentionPolicy.CLASS)
@Target(TYPE)
@Repeatable(ConnectorAttributes.class)
public @interface ConnectorAttribute {

    /**
     * Attribute direction.
     */
    enum Direction {
        /**
         * Attribute related to an incoming connector, like a deserializer.
         */
        INCOMING,
        /**
         * Attribute related to an outgoing connector, like a serializer.
         */
        OUTGOING,
        /**
         * Attribute used for both directions.
         */
        INCOMING_AND_OUTGOING
    }

    /**
     * The constant used to indicate that the attribute has no default value or no alias.
     */
    String NO_VALUE = "<no-value>";

    /**
     * @return the attribute name, must not be {@code null}, must not be {@code blank}, must be unique for a specific
     *         connector
     */
    String name();

    /**
     * @return the description of the attribute.
     */
    String description();

    /**
     * @return whether the attribute must be hidden.
     */
    boolean hidden() default false;

    /**
     * @return whether the attribute is mandatory.
     */
    boolean mandatory() default false;

    /**
     * @return on which direction the attribute is used.
     */
    Direction direction();

    /**
     * @return the default value if any.
     */
    String defaultValue() default NO_VALUE;

    /**
     * @return whether the attribute is deprecated.
     */
    boolean deprecated() default false;

    /**
     * @return the optional MicroProfile Config property used to configure the attribute.
     */
    String alias() default NO_VALUE;

    /**
     * @return the java type of the property.
     */
    String type();
}
