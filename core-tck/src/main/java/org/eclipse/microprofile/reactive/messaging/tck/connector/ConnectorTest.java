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
package org.eclipse.microprofile.reactive.messaging.tck.connector;

import org.eclipse.microprofile.reactive.messaging.spi.ConnectorLiteral;
import org.eclipse.microprofile.reactive.messaging.tck.ArchiveExtender;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import java.util.ServiceLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * This test deploys a dummy connector to ensure that the implementation creates the instances.
 */
@RunWith(Arquillian.class)
public class ConnectorTest {

    @Inject
    private BeanManager manager;

    @Deployment
    public static Archive<JavaArchive> deployment() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
            .addClasses(DummyConnector.class, MyProcessor.class, FakeConfig.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(archive));
        return archive;
    }

    @Test
    public void checkConnector() {
        DummyConnector connector = manager.createInstance()
            .select(DummyConnector.class, ConnectorLiteral.of("Dummy")).get();
        await().until(() -> connector.elements().size() == 10);
        assertThat(connector.elements()).containsExactly("A", "B", "C", "D", "E", "F", "G", "H", "I", "J");

        assertThat(connector.getReceivedConfigurations()).hasSize(3).allSatisfy(config -> {
            assertThat(config.getValue("common-A", String.class)).isEqualTo("Value-A");
            assertThat(config.getValue("common-B", String.class)).isEqualTo("Value-B");
        });
    }

}
