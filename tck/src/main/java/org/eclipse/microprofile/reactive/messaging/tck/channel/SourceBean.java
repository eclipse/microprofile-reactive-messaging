/*
 * Copyright (c) 2020, 2021 Contributors to the Eclipse Foundation
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
package org.eclipse.microprofile.reactive.messaging.tck.channel;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.ProcessorBuilder;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.reactivestreams.Publisher;

import io.reactivex.Flowable;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SourceBean {

    @Outgoing("hello")
    public Publisher<String> hello() {
        return Flowable.fromArray("h", "e", "l", "l", "o");
    }

    @Outgoing("bonjour")
    @Incoming("raw")
    public ProcessorBuilder<String, Object> bonjour() {
        return ReactiveStreams.<String>builder().map(String::toUpperCase);
    }

    @Outgoing("raw")
    public PublisherBuilder<String> raw() {
        return ReactiveStreams.of("b", "o", "n", "j", "o", "u", "r");
    }

}
