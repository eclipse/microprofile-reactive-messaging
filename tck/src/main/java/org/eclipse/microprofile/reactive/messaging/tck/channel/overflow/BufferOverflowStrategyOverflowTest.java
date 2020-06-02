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

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.stream.IntStream;

import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.tck.TckBase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;


public class BufferOverflowStrategyOverflowTest extends TckBase {

    @Deployment
    public static Archive<JavaArchive> deployment() {
        return getBaseArchive()
            .addClasses(BeanUsingBufferOverflowStrategy.class);
    }

    @Inject
    private BeanUsingBufferOverflowStrategy bean;
    

    @Test
    public void testOverflow() {
        
        bean.tryEmitThousand();

        assertThat(bean.accepted().size() + bean.rejected().size()).isEqualTo(1000);
        assertThat(bean.rejected()).isNotEmpty();
        
        // Buffer size is 300, so first 300 items should always be accepted
        assertThat(bean.accepted()).containsAll(IntStream.range(0, 300).mapToObj(Integer::toString).collect(toList()));
        
        await().until(() -> bean.output().size() == bean.accepted().size());
        assertThat(bean.accepted()).containsExactlyElementsOf(bean.accepted());
        assertThat(bean.failure()).isNull();
    }
}
