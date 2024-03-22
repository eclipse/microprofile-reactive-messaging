/*
 * Copyright (c) 2020, 2021 Contributors to the Eclipse Foundation
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

import org.eclipse.microprofile.reactive.messaging.tck.TckBase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

import jakarta.inject.Inject;

public class FailOverflowStrategyOverflowTest extends TckBase {

    @Deployment
    public static Archive<JavaArchive> deployment() {
        return getBaseArchive()
                .addClasses(BeanWithFailOverflowStrategy.class);
    }

    private @Inject BeanWithFailOverflowStrategy bean;

    @Test
    public void testOverflow() {
        bean.emitALotOfItems();

        await().until(() -> bean.failure() != null);
        assertThat(bean.failure()).isInstanceOf(Exception.class);
        assertThat(bean.output()).doesNotContain("999");
        assertThat(bean.output()).hasSizeLessThan(999);
        assertThat(bean.exception()).isInstanceOf(IllegalStateException.class);

    }
}
