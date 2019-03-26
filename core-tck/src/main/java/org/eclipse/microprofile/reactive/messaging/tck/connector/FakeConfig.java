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
import org.eclipse.microprofile.config.spi.ConfigSource;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.util.*;

/**
 * Bean used to produce a MicroProfile Config avoiding the TCK to depends on a specific implementation.
 * The produced Config is equivalent to:
 * <pre>
 *      ## Dummy Incoming connector
 *      mp.messaging.incoming.dummy-source.type=org.eclipse.microprofile.reactive.messaging.tck.connector.Dummy
 *      mp.messaging.incoming.dummy-source.attribute=value
 *      mp.messaging.incoming.dummy-source.items=a,b,c,d,e,f,g,h,i,j
 *
 *      ## Dummy Outgoing connector
 *      mp.messaging.outgoing.dummy-sink.type=org.eclipse.microprofile.reactive.messaging.tck.connector.Dummy
 *      mp.messaging.outgoing.dummy-sink.attribute=value
 * </pre>
 */
@SuppressWarnings("unchecked")
@ApplicationScoped
public class FakeConfig {


    @Produces
    public Config config() {
        Map<String, String> backend = new HashMap<>();
        backend.put("mp.messaging.incoming.dummy-source.type" , Dummy.class.getName());
        backend.put("mp.messaging.incoming.dummy-source.attribute" , "value");
        backend.put("mp.messaging.incoming.dummy-source.items", "a,b,c,d,e,f,g,h,i,j");
        backend.put("mp.messaging.outgoing.dummy-sink.type" , Dummy.class.getName());
        backend.put("mp.messaging.outgoing.dummy-sink.attribute" , "value");


        return new Config() {
            @Override
            public <T> T getValue(String propertyName, Class<T> propertyType) {
                T value = (T) backend.get(propertyName);
                if (value == null) {
                    throw new NoSuchElementException(propertyName);
                }
                return value;
            }

            @Override
            public <T> Optional<T> getOptionalValue(String propertyName, Class<T> propertyType) {
                T value = (T) backend.get(propertyName);
                if (value == null) {
                    return Optional.empty();
                }
                return Optional.of(value);
            }

            @Override
            public Iterable<String> getPropertyNames() {
                return backend.keySet();
            }

            @Override
            public Iterable<ConfigSource> getConfigSources() {
                return Collections.emptyList();
            }
        };
    }


}
