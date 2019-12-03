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

import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.tck.TckBase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;


public class EmitterInjectionMessageBeanWithPayloadsWithAckTest extends TckBase {

    @Deployment
    public static Archive<JavaArchive> deployment() {
        return getBaseArchive()
            .addClasses(MyMessageBeanEmittingPayloadsWithAck.class);
    }

    private @Inject MyMessageBeanEmittingPayloadsWithAck myMessageBeanEmittingPayloadsWithAck;
    @Test
    public void testMyMessageBeanWithPayloadsAndAck() {
        
        myMessageBeanEmittingPayloadsWithAck.run();
        assertThat(myMessageBeanEmittingPayloadsWithAck.emitter()).isNotNull();
        assertThat(myMessageBeanEmittingPayloadsWithAck.list()).containsExactly("a", "b", "c");
        assertThat(myMessageBeanEmittingPayloadsWithAck.emitter().isCancelled()).isTrue();
        assertThat(myMessageBeanEmittingPayloadsWithAck.emitter().hasRequests()).isFalse();
    }


}
