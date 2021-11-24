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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment.Strategy;

@ApplicationScoped
public class MyBeanEmittingPayloadsWithAck {
    @Inject
    @Channel("foo")
    private Emitter<String> emitter;
    private final List<String> list = new CopyOnWriteArrayList<>();

    private final List<CompletionStage<Void>> csList = new CopyOnWriteArrayList<>();

    public Emitter<String> emitter() {
        return emitter;
    }

    public List<String> list() {
        return list;
    }

    public void run() {
        csList.add(emitter.send("a"));
        csList.add(emitter.send("b"));
        csList.add(emitter.send("c"));
        emitter.complete();
    }

    List<CompletionStage<Void>> getCompletionStage() {
        return csList;
    }

    @Incoming("foo")
    @Acknowledgment(Strategy.MANUAL)
    public CompletionStage<Void> consume(final Message<String> s) {
        list.add(s.getPayload());

        if (!"c".equals(s.getPayload())) {
            return s.ack();
        }
        else {
            return new CompletableFuture<>();

        }

    }
}
