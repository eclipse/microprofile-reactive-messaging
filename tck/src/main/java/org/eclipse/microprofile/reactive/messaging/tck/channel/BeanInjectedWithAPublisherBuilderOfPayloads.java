/**
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

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;

import io.reactivex.Flowable;


@ApplicationScoped
public class BeanInjectedWithAPublisherBuilderOfPayloads {

    private final PublisherBuilder<String> constructor;
    @Inject
    @Channel("hello")
    private PublisherBuilder<String> field;

    public BeanInjectedWithAPublisherBuilderOfPayloads() {
        this.constructor = null;
    }

    @Inject
    public BeanInjectedWithAPublisherBuilderOfPayloads(@Channel("bonjour") PublisherBuilder<String> constructor) {
        this.constructor = constructor;
    }

    public List<String> consume() {
        return Flowable.fromPublisher(
                ReactiveStreams.concat(constructor, field).buildRs())
                .toList()
                .blockingGet();
    }

}
