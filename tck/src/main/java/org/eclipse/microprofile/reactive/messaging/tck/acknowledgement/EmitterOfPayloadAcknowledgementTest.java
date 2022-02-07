/*
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.tck.ArchiveExtender;
import org.eclipse.microprofile.reactive.messaging.tck.TckBase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

public class EmitterOfPayloadAcknowledgementTest extends TckBase {

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
    public void testThatEmitterReceiveAcksAfterSuccessfulProcessingOfPayload() {
        processor.disableFailureMode();
        Emitter<String> emitter = bean.getEmitter();

        CompletableFuture<Void> all = CompletableFuture.allOf(
                emitter.send("a").toCompletableFuture(),
                emitter.send("b").toCompletableFuture(),
                emitter.send("c").toCompletableFuture(),
                emitter.send("d").toCompletableFuture(),
                emitter.send("e").toCompletableFuture());

        await().until(all::isDone);
        assertThat(all.isCompletedExceptionally()).isFalse();
        assertThat(all.isCancelled()).isFalse();
    }

    @Test
    public void testThatEmitterReceiveNacksAfterFailingProcessingOfPayload() {
        Emitter<String> emitter = bean.getEmitter();
        processor.enableFailureMode();

        emitter.send("a").toCompletableFuture().join();
        assertThatThrownBy(() -> emitter.send("b").toCompletableFuture().join())
                .hasRootCauseInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> emitter.send("c").toCompletableFuture().join())
                .hasRootCauseInstanceOf(IllegalArgumentException.class);
        emitter.send("d").toCompletableFuture().join();
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

        @Incoming("data")
        public CompletionStage<Void> process(String s) {
            if (failureModeEnabled) {
                if (s.equalsIgnoreCase("b")) {
                    // nacked
                    throw new IllegalArgumentException("b");
                }

                if (s.equalsIgnoreCase("c")) {
                    CompletableFuture<Void> cf = new CompletableFuture<>();
                    cf.completeExceptionally(new IllegalArgumentException("c"));
                    return cf;
                }
            }
            return CompletableFuture.completedFuture(null);
        }
    }
}
