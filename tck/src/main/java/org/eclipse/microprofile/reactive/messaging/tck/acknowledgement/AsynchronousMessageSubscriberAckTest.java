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

import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
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

public class AsynchronousMessageSubscriberAckTest extends TckBase {

    @Deployment
    public static Archive<JavaArchive> deployment() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
            .addClasses(EmitterBean.class, MessageConsumer.class, ArchiveExtender.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(archive));
        return archive;
    }

    @Inject
    private EmitterBean bean;

    @Inject
    private MessageConsumer processor;


    @Test
    public void testThatMessagesAreAckedAfterSuccessfulProcessingOfMessage() throws InterruptedException, TimeoutException, ExecutionException {
        processor.disableFailureMode();
        processor.reset();
        Emitter<String> emitter = bean.getEmitter();

        Set<String> acked = ConcurrentHashMap.newKeySet();
        Set<String> nacked = ConcurrentHashMap.newKeySet();

        assertThat(run(acked, nacked, emitter)).isEmpty();

        await().until(() -> processor.list().size() == 10);
        assertThat(acked).hasSize(10);
        assertThat(nacked).hasSize(0);
    }

    @Test
    public void testThatMessagesAreNackedAfterFailingProcessingOfMessage() throws InterruptedException, TimeoutException, ExecutionException {
        Emitter<String> emitter = bean.getEmitter();

        Set<String> acked = ConcurrentHashMap.newKeySet();
        Set<String> nacked = ConcurrentHashMap.newKeySet();

        processor.enableFailureMode();
        processor.reset();

        List<Throwable> throwables = run(acked, nacked, emitter);

        await().until(() -> processor.list().size() == 8);
        assertThat(acked).hasSize(8);
        assertThat(nacked).hasSize(2);
        assertThat(throwables).hasSize(2);
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
    public static class MessageConsumer {

        private boolean failureModeEnabled = false;

        public void enableFailureMode() {
            failureModeEnabled = true;
        }

        public void disableFailureMode() {
            failureModeEnabled = false;
        }

        private final List<String> list = new CopyOnWriteArrayList<>();

        @Incoming("data")
        @Acknowledgment(Acknowledgment.Strategy.POST_PROCESSING)
        public CompletionStage<Void> consume(Message<String> m) {
            String s = m.getPayload();
            if (failureModeEnabled) {
                if (s.equalsIgnoreCase("b")) {
                    CompletableFuture<Void> future = new CompletableFuture<>();
                    future.completeExceptionally(new IllegalArgumentException("b"));
                    return future;
                }
                else if (s.equalsIgnoreCase("h")) {
                    throw new IllegalStateException("h");
                }
            }
            list.add(s);
            return m.ack();
        }

        public List<String> list() {
            return list;
        }

        public void reset() {
            list.clear();
        }
    }

}
