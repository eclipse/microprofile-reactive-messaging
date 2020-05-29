/**
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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
package org.eclipse.microprofile.reactive.messaging.tck.channel.overflow;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;

@ApplicationScoped
public class BeanUsingThrowExceptionStrategy {
    
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    @PreDestroy
    public void terminate() {
        executor.shutdown();
    } 

    @Inject
    @Channel("hello")
    @OnOverflow(value = OnOverflow.Strategy.THROW_EXCEPTION)
    private Emitter<String> emitter;

    private final List<String> output = new CopyOnWriteArrayList<>();
    private final List<String> accepted = new CopyOnWriteArrayList<>();
    private final List<String> rejected = new CopyOnWriteArrayList<>();

    private volatile Throwable downstreamFailure;

    public List<String> output() {
        return output;
    }

    public List<String> accepted() {
        return accepted;
    }

    public List<String> rejected() {
        return rejected;
    }

    public Throwable failure() {
        return downstreamFailure;
    }

    public void tryEmitOne() {
        tryEmit("1");
    }

    public void tryEmitTen() {
        for (int i = 0; i < 10; i++) {
            tryEmit(Integer.toString(i));
        }
    }
    
    private void tryEmit(String item) {
        try {
            emitter.send(item);
            accepted.add(item);
        }
        catch (IllegalStateException e) {
            rejected.add(item);
        }
    }
    
    @Incoming("hello")
    @Outgoing("out")
    public PublisherBuilder<String> consume(final PublisherBuilder<String> values) {
        return values.via(ReactiveStreams.<String>builder().flatMapCompletionStage(s -> CompletableFuture.supplyAsync(()-> {
            try {
                Thread.sleep(1000); 
            } 
            catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            return s;
        }, executor))).onError(err -> downstreamFailure = err);
    }

    @Incoming("out")
    public void out(final String s) {
        output.add(s);
    }

}