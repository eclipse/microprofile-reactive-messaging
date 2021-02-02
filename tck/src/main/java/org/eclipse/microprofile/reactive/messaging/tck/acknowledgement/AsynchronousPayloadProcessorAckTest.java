/**
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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
package org.eclipse.microprofile.reactive.messaging.tck.acknowledgement;

import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.messaging.tck.ArchiveExtender;
import org.eclipse.microprofile.reactive.messaging.tck.TckBase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class AsynchronousPayloadProcessorAckTest extends TckBase {

    @Deployment
    public static Archive<JavaArchive> deployment() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
            .addClasses(EmitterBean.class, Sink.class, MessageProcessor.class, ArchiveExtender.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(archive));
        return archive;
    }

    @Inject
    private EmitterBean bean;

    @Inject
    private MessageProcessor processor;

    @Inject
    private Sink sink;

    @Test
    public void testThatMessagesAreAckedAfterSuccessfulProcessingOfPayload() throws InterruptedException, TimeoutException, ExecutionException {
        sink.reset();
        processor.disableFailureMode();
        Emitter<String> emitter = bean.getEmitter();

        Set<String> acked = ConcurrentHashMap.newKeySet();
        Set<String> nacked = ConcurrentHashMap.newKeySet();

        run(acked, nacked, emitter);

        await().until(() -> sink.list().size() == 10);
        assertThat(acked).hasSize(10);
        assertThat(nacked).hasSize(0);
    }

    @Test
    public void testThatMessagesAreNackedAfterFailingProcessingOfPayload() throws InterruptedException, TimeoutException, ExecutionException {
        sink.reset();
        Emitter<String> emitter = bean.getEmitter();
        processor.enableFailureMode();

        Set<String> acked = ConcurrentHashMap.newKeySet();
        Set<String> nacked = ConcurrentHashMap.newKeySet();

        List<Throwable> throwables = run(acked, nacked, emitter);

        await().until(() -> sink.list().size() == 7);
        assertThat(acked).hasSize(7);
        assertThat(nacked).hasSize(3);
        assertThat(throwables).hasSize(3);
    }


    private List<Throwable> run(Set<String> acked, Set<String> nacked, Emitter<String> emitter)
        throws InterruptedException, TimeoutException, ExecutionException {
        List<Throwable> reasons = new CopyOnWriteArrayList<>();
        CompletableFuture.allOf(Stream.of("a", "b", "c", "d", "e", "f", "g", "h", "i", "j")
            .map(i ->
                CompletableFuture.runAsync(() -> emitter.send(Message.of(i,
                    () -> {
                        acked.add(i);
                        return CompletableFuture.completedFuture(null);
                    }, t -> {
                        reasons.add(t);
                        nacked.add(i);
                        return CompletableFuture.completedFuture(null);
                    })))
                    .thenApply(x -> i)).toArray(CompletableFuture[]::new))
            .get(10, TimeUnit.SECONDS);

        return reasons;
    }


    @ApplicationScoped
    public static class Sink {
        private final List<String> list = new CopyOnWriteArrayList<>();

        @Incoming("out")
        public void consume(String s) {
            list.add(s);
        }

        public List<String> list() {
            return list;
        }

        public void reset() {
            list.clear();
        }
    }

    @ApplicationScoped
    public static class MessageProcessor {


        private boolean failureModeEnabled = false;

        public void enableFailureMode() {
            failureModeEnabled = true;
        }

        public void disableFailureMode() {
            failureModeEnabled = false;
        }

        @Incoming("data")
        @Outgoing("out")
        public CompletionStage<String> process(String s) {
            if (failureModeEnabled) {
                if (s.equalsIgnoreCase("b")) {
                    // nacked
                    throw new IllegalArgumentException("b");
                }

                if (s.equalsIgnoreCase("f")) {
                    // nacked - must not return `null`
                    return null;
                }

                if (s.equalsIgnoreCase("c")) {
                    CompletableFuture<String> cf = new CompletableFuture<>();
                    cf.completeExceptionally(new IllegalArgumentException("c"));
                    return cf;
                }
            }
            return CompletableFuture.completedFuture(s.toUpperCase());
        }
    }
}
