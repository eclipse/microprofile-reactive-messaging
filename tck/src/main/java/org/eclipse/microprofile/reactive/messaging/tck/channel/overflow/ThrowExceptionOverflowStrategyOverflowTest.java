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

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.tck.TckBase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

public class ThrowExceptionOverflowStrategyOverflowTest extends TckBase {

    @Deployment
    public static Archive<JavaArchive> deployment() {
        return getBaseArchive()
            .addClasses(BeanUsingThrowExceptionStrategy.class);
    }

    private @Inject BeanUsingThrowExceptionStrategy bean;

    @Test
    public void testOverflow() throws InterruptedException {
        
        bean.tryEmitTen();

        // Assert all items either accepted or rejected
        assertThat(bean.accepted().size() + bean.rejected().size()).isEqualTo(10);
        // At least the first item should have been accepted
        assertThat(bean.accepted()).contains("1");
        // But not everything should have been accepted
        assertThat(bean.rejected()).isNotEmpty();
        
        // Everything accepted should eventually be processed
        await().until(() -> bean.output().size() == bean.accepted().size());
        assertThat(bean.output()).containsExactlyElementsOf(bean.accepted());
        assertThat(bean.failure()).isNull();
        
        int acceptedFirstRun = bean.accepted().size();
        int rejectedFirstRun = bean.rejected().size();
        
        // Stream should still be running, so we should be able to test this again
        bean.tryEmitTen();
        
        await().until(() -> bean.accepted().size() + bean.rejected().size() == 20);
        assertThat(bean.accepted()).hasSizeGreaterThan(acceptedFirstRun);
        assertThat(bean.rejected()).hasSizeGreaterThan(rejectedFirstRun);
        
        await().until(() -> bean.output().size() == bean.accepted().size());
        assertThat(bean.output()).containsExactlyElementsOf(bean.accepted());
        assertThat(bean.failure()).isNull();
    }

}
