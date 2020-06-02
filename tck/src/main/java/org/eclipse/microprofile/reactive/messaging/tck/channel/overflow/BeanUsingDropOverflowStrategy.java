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
public class BeanUsingDropOverflowStrategy {
  

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
  
    @PreDestroy
    public void terminate() {
        executor.shutdown();
    } 
    @Inject
    @Channel("hello")
    @OnOverflow(value = OnOverflow.Strategy.DROP)
    private Emitter<String> emitter;

    private List<String> output = new CopyOnWriteArrayList<>();

    private volatile Throwable downstreamFailure;
    private volatile boolean done;
    private Exception callerException;

    public boolean isDone() {
        return done;
    }

    public List<String> output() {
        return output;
    }

    public Throwable failure() {
        return downstreamFailure;
    }

    public Exception exception() {
        return callerException;
    }

    public void emitThree() {
        try {
            emitter.send("1");
            emitter.send("2");
            emitter.send("3");
            emitter.complete();
        } 
        catch (Exception e) {
            callerException = e;
        }
    }

    public void emitALotOfItems() {
        new Thread(() -> {
            try {
                for (int i = 1; i < 1000; i++) {
                    emitter.send("" + i);
                }
            } 
            catch (Exception e) {
                callerException = e;
            } 
            finally {
                done = true;
            }
        }).start();
    }

    @Incoming("hello")
    @Outgoing("out")
    public PublisherBuilder<String> consume(final PublisherBuilder<String> values) {
        
        return values.via(ReactiveStreams.<String>builder().flatMapCompletionStage(s -> CompletableFuture.supplyAsync(()-> {
            try {
                Thread.sleep(1); 
            } 
            catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            return s;
        }, executor))).onError(err -> downstreamFailure = err);
        
    }


    @Incoming("out")
    public void out(String s) {
        output.add(s);
    }

}