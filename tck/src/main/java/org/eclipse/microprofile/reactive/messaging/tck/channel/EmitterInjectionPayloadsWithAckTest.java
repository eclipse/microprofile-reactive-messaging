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

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.List;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.tck.TckBase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;


public class EmitterInjectionPayloadsWithAckTest extends TckBase {

    @Deployment
    public static Archive<JavaArchive> deployment() {
        return getBaseArchive()
            .addClasses(MyBeanEmittingPayloadsWithAck.class);
    }

    private @Inject MyBeanEmittingPayloadsWithAck myBeanEmittingPayloadsWithAck;
    @Test
    public void testWithPayloadsAndAck() {
        
        myBeanEmittingPayloadsWithAck.run();
        List<CompletionStage<Void>> cs = myBeanEmittingPayloadsWithAck.getCompletionStage();
        assertThat(myBeanEmittingPayloadsWithAck.emitter()).isNotNull();
        assertThat(cs.get(0).toCompletableFuture().isDone()).isTrue();
        assertThat(cs.get(1).toCompletableFuture().isDone()).isTrue();
        assertThat(cs.get(2).toCompletableFuture().isDone()).isFalse();
        await().until(() -> myBeanEmittingPayloadsWithAck.list().size() == 3);
        assertThat(myBeanEmittingPayloadsWithAck.list()).containsExactly("a", "b", "c");
        assertThat(myBeanEmittingPayloadsWithAck.emitter().isCancelled()).isTrue();
        assertThat(myBeanEmittingPayloadsWithAck.emitter().hasRequests()).isFalse();
    }

}
