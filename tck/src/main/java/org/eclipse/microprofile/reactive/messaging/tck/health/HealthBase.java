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
package org.eclipse.microprofile.reactive.messaging.tck.health;

import java.net.URI;
import java.util.ServiceLoader;

import org.eclipse.microprofile.reactive.messaging.tck.ArchiveExtender;
import org.eclipse.microprofile.reactive.messaging.tck.metrics.ConfigAsset;
import org.eclipse.microprofile.reactive.messaging.tck.metrics.TestConnector;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class HealthBase {

    public static final int TIMEOUT_MILLIS = 5000;

    public static final String ALL_EXCLUDED_CHANNEL = "excluded-channel";
    public static final String LIVE_EXCLUDED_CHANNEL = "live-excluded-channel";
    public static final String READY_EXCLUDED_CHANNEL = "ready-excluded-channel";
    public static final String CHANNEL_CONNECTOR_IN = "channel-connector-in";
    public static final String CHANNEL_CONNECTOR_OUT = "channel-connector-out";
    public static final String CHANNEL_INNER = "inner-channel";

    protected static WebArchive prepareArchive(Class<?>... classes) {
        ConfigAsset config = new ConfigAsset()
            .put("mp.messaging.incoming.channel-connector-in.connector", TestConnector.ID)
            .put("mp.messaging.outgoing.channel-connector-out.connector", TestConnector.ID)
            .put("mp.messaging.health.ready.exclude", READY_EXCLUDED_CHANNEL + "," + ALL_EXCLUDED_CHANNEL)
            .put("mp.messaging.health.live.exclude", LIVE_EXCLUDED_CHANNEL + "," + ALL_EXCLUDED_CHANNEL);

        JavaArchive testJar = ShrinkWrap
            .create(JavaArchive.class, "healthTest.jar")
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
            .addAsManifestResource(config, "microprofile-config.properties")
            .addClasses(HealthTestBean.class, HealthTestConnector.class, ChannelRegister.class)
            .addClasses(classes)
            .as(JavaArchive.class);

        ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(testJar));

        return ShrinkWrap
            .create(WebArchive.class, "healthTest.war")
            .addAsLibrary(testJar);
    }

    @ArquillianResource
    private URI uri;

    protected HealthAssertions getLiveness() {
        return HealthAssertions.create(uri + "/health/live");
    }

    protected HealthAssertions getReadiness() {
        return HealthAssertions.create(uri + "/health/ready");
    }

}
