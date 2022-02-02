/*
 * Copyright (c) 2018, 2021 Contributors to the Eclipse Foundation
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
package org.eclipse.microprofile.reactive.messaging.tck.metrics;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.ServiceLoader;

import org.awaitility.Awaitility;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricRegistry.Type;
import org.eclipse.microprofile.metrics.annotation.RegistryType;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.eclipse.microprofile.reactive.messaging.tck.ArchiveExtender;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.inject.Inject;

@RunWith(Arquillian.class)
public class MetricsTest {

    @Deployment
    public static JavaArchive deployment() {
        ConfigAsset config = new ConfigAsset()
                .put("mp.messaging.incoming.channel-connector-in.connector", TestConnector.ID)
                .put("mp.messaging.outgoing.channel-connector-out.connector", TestConnector.ID);

        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
                .addClasses(MetricsTestBean.class, TestConnector.class, ArchiveExtender.class)
                .addAsResource(config, "META-INF/microprofile-config.properties")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(archive));

        return archive;
    }

    @Inject
    @Connector(TestConnector.ID)
    private TestConnector testConnector;

    @Inject
    @RegistryType(type = Type.BASE)
    private MetricRegistry metricRegistry;

    @Inject
    private MetricsTestBean testBean;

    @Test
    public void testMetricsConnector() {
        testConnector.send(MetricsTestBean.CONNECTOR_IN, Message.of("one"));
        testConnector.send(MetricsTestBean.CONNECTOR_IN, Message.of("two"));

        assertEquals("one-test-1", testConnector.get(MetricsTestBean.CONNECTOR_OUT).getPayload());
        assertEquals("one-test-2", testConnector.get(MetricsTestBean.CONNECTOR_OUT).getPayload());
        assertEquals("two-test-1", testConnector.get(MetricsTestBean.CONNECTOR_OUT).getPayload());
        assertEquals("two-test-2", testConnector.get(MetricsTestBean.CONNECTOR_OUT).getPayload());

        Counter channelInCounter = getMessageCounterForChannel(MetricsTestBean.CONNECTOR_IN);
        Counter channelProcessCounter = getMessageCounterForChannel(MetricsTestBean.CONNECTOR_PROCESS);
        Counter channelOutCounter = getMessageCounterForChannel(MetricsTestBean.CONNECTOR_OUT);

        assertEquals(2, channelInCounter.getCount());
        assertEquals(2, channelProcessCounter.getCount());
        assertEquals(4, channelOutCounter.getCount());
    }

    @Test
    public void testMetricsInApp() {
        Awaitility.await().until(testBean::getInAppMessagesReceived, equalTo(6));

        Counter appACounter = getMessageCounterForChannel(MetricsTestBean.CHANNEL_APP_A);
        Counter appBCounter = getMessageCounterForChannel(MetricsTestBean.CHANNEL_APP_B);

        assertEquals(3, appACounter.getCount());
        assertEquals(6, appBCounter.getCount());
    }

    private Counter getMessageCounterForChannel(String channel) {
        Map<MetricID, Counter> counters =
                metricRegistry.getCounters((id, m) -> id.getName().equals("mp.messaging.message.count")
                        && id.getTags().getOrDefault("channel", "").equals(channel));

        assertThat(counters.entrySet(), hasSize(1));

        return counters.values().iterator().next();
    }

}
