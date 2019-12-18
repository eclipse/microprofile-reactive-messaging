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
package org.eclipse.microprofile.reactive.messaging.tck.metrics;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.eclipse.microprofile.reactive.messaging.spi.IncomingConnectorFactory;
import org.eclipse.microprofile.reactive.messaging.spi.OutgoingConnectorFactory;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.eclipse.microprofile.reactive.streams.operators.SubscriberBuilder;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;

@ApplicationScoped
@Connector(TestConnector.ID)
public class TestConnector implements IncomingConnectorFactory, OutgoingConnectorFactory {
    
    public static final String ID = "test-connector";
    
    private Map<String, FlowableEmitter<Message<String>>> incomingEmitters = new HashMap<>();
    private Map<String, LinkedBlockingQueue<Message<String>>> outgoingQueues = new HashMap<>();

    @Override
    public SubscriberBuilder<? extends Message<String>, Void> getSubscriberBuilder(Config config) {
        String channel = config.getValue(CHANNEL_NAME_ATTRIBUTE, String.class);
        LinkedBlockingQueue<Message<String>> queue = new LinkedBlockingQueue<>();
        outgoingQueues.put(channel, queue);
        return ReactiveStreams.<Message<String>>builder().forEach(queue::add);
    }

    @Override
    public PublisherBuilder<? extends Message<String>> getPublisherBuilder(Config config) {
        String channel = config.getValue(CHANNEL_NAME_ATTRIBUTE, String.class);
        Flowable<Message<String>> flowable = Flowable.create((e) -> incomingEmitters.put(channel, e), BackpressureStrategy.BUFFER);
        return ReactiveStreams.fromPublisher(flowable);
    }
    
    public void send(String channel, Message<String> message) {
        FlowableEmitter<Message<String>> emitter = incomingEmitters.get(channel);
        
        if (emitter == null) {
            throw new RuntimeException("No such incoming channel registered: " + channel);
        }
        
        emitter.onNext(message);
    }
    
    public Message<String> get(String channel) {
        LinkedBlockingQueue<Message<String>> queue = outgoingQueues.get(channel);
        
        if (queue == null) {
            throw new RuntimeException("No such outgoing channel registered: " + channel);
        }
        
        Message<String> result = null;
        try {
            result = queue.poll(5, SECONDS);
        }
        catch (InterruptedException e) {
            fail("Interrupted while waiting for messages");
        }
        
        if (result == null) {
            fail("Timed out waiting for messages");
        }
        
        return result;
    }
    
}
