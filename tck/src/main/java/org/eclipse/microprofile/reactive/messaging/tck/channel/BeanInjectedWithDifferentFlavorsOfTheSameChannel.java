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

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.reactivestreams.Publisher;

import io.reactivex.Flowable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class BeanInjectedWithDifferentFlavorsOfTheSameChannel {

    @Outgoing("hello-1")
    public Publisher<String> hello1() {
        return Flowable.fromArray("h", "e", "l", "l", "o");
    }

    @Outgoing("hello-2")
    public Publisher<String> hello2() {
        return Flowable.fromArray("h", "e", "l", "l", "o");
    }

    @Outgoing("hello-3")
    public Publisher<String> hello3() {
        return Flowable.fromArray("h", "e", "l", "l", "o");
    }

    @Outgoing("hello-4")
    public Publisher<String> hello4() {
        return Flowable.fromArray("h", "e", "l", "l", "o");
    }

    @Outgoing("hello-5")
    public Publisher<String> hello5() {
        return Flowable.fromArray("h", "e", "l", "l", "o");
    }

    @Outgoing("hello-6")
    public Publisher<String> hello6() {
        return Flowable.fromArray("h", "e", "l", "l", "o");
    }

    @Inject
    @Channel("hello-1")
    private Publisher<Message<String>> field1;

    @Inject
    @Channel("hello-2")
    private Publisher<Message> field2;

    @Inject
    @Channel("hello-3")
    private PublisherBuilder<Message> field3;

    @Inject
    @Channel("hello-4")
    private PublisherBuilder<Message<String>> field4;

    @Inject
    @Channel("hello-5")
    private PublisherBuilder<String> field5;

    @Inject
    @Channel("hello-6")
    private Publisher<String> field6;

    public Map<String, String> consume() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("1", field1.toString());
        map.put("2", field2.toString());
        map.put("3", field3.toString());
        map.put("4", field4.toString());
        map.put("5", field5.toString());
        map.put("6", field6.toString());
        return map;
    }

}
