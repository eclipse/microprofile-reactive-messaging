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
package org.eclipse.microprofile.reactive.messaging.tck.channel.overflow;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@ApplicationScoped
public class BeanUsingBufferOverflowWithoutBufferSizeStrategy {


    @Inject
    @Channel("hello")
    @OnOverflow(value = OnOverflow.Strategy.BUFFER)
    private Emitter<String> emitter;

    private final List<String> output = new CopyOnWriteArrayList<>();

    private volatile Throwable downstreamFailure;
    private Exception callerException;

    public List<String> output() {
        return output;
    }

    public Throwable failure() {
        return downstreamFailure;
    }

    public Exception exception() {
        return callerException;
    }

    public void emitALotOfItems() {
        new Thread(() -> {
            try {
                for (int i = 1; i < 1000; i++) {
                    String message = "" + i;
                    emitter.send(message);
                    output.add(message);
                }
            } 
            catch (final Exception e) {
                callerException = e;
            }
        }).start();
    }

    @Incoming("hello")
    public Subscriber<String> consume() {
        // create a subscriber sitting there and doing nothing
        return new Subscriber<String>() {

            @Override
            public void onSubscribe(Subscription s) {
                
            }

            @Override
            public void onNext(String t) {
                
            }

            @Override
            public void onError(Throwable t) {
                downstreamFailure = t;
            }

            @Override
            public void onComplete() {
                
            }

       };
    }

  

}