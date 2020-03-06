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
package org.eclipse.microprofile.reactive.messaging.tck.channel;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

@ApplicationScoped
public class MyBeanEmittingNull {
    @Inject
    @Channel("foo")
    private Emitter<String> emitter;
    private final List<String> list = new CopyOnWriteArrayList<>();
    private boolean caughtNullPayload;
    private boolean caughtNullMessage;

    public Emitter<String> emitter() {
        return emitter;
    }

    boolean hasCaughtNullPayload() {
        return caughtNullPayload;
    }

    boolean hasCaughtNullMessage() {
        return caughtNullMessage;
    }

    public List<String> list() {
        return list;
    }

    public void run() {
        emitter.send("a");
        emitter.send("b");
        try {
            emitter.send((String) null);
        } 
        catch (IllegalArgumentException e) {
            caughtNullPayload = true;
        }

        try {
            emitter.send((Message<String>) null);
        } 
        catch (IllegalArgumentException e) {
            caughtNullMessage = true;
        }
        emitter.send("c");
        emitter.complete();
    }

    @Incoming("foo")
    public void consume(final String s) {
        list.add(s);
    }
}