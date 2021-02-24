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

import org.eclipse.microprofile.reactive.messaging.tck.metrics.ConfigAsset;
import org.eclipse.microprofile.reactive.messaging.tck.metrics.TestConnector;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class HealthBase {

    public static final String ALL_EXCLUDED_CHANNEL = "excluded-channel";
    public static final String LIVE_EXCLUDED_CHANNEL = "live-excluded-channel";
    public static final String READY_EXCLUDED_CHANNEL = "ready-excluded-channel";
    public static final String CHANNEL_CONNECTOR_IN = "channel-connector-in";
    public static final String CHANNEL_CONNECTOR_OUT = "channel-connector-out";
    public static final String CHANNEL_INNER = "inner-channel";

    protected static WebArchive prepareArchive(){
        ConfigAsset config = new ConfigAsset()
            .put("mp.messaging.incoming.channel-connector-in.connector", TestConnector.ID)
            .put("mp.messaging.outgoing.channel-connector-out.connector", TestConnector.ID)
            .put("mp.health.ready.exclude-channel", READY_EXCLUDED_CHANNEL + "," + ALL_EXCLUDED_CHANNEL)
            .put("mp.health.live.exclude-channel", LIVE_EXCLUDED_CHANNEL + "," + ALL_EXCLUDED_CHANNEL);

        return ShrinkWrap.create(WebArchive.class, HealthExclusionTest.class.getName() + ".war")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
            .addAsResource(config, "META-INF/microprofile-config.properties")
            .addClasses(HealthTestBean.class, HealthTestConnector.class, ChannelRegister.class);
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
