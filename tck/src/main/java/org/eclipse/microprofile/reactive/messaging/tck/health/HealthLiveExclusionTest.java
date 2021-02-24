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

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class HealthLiveExclusionTest extends HealthBase{

    @Inject
    private ChannelRegister channelRegister;

    @Deployment(name = "HealthLiveExclusionTest")
    public static WebArchive deployment() {
        return prepareArchive().addClass(HealthLiveExcludedTestBean.class);
    }

    @Test
    @InSequence(1)
    @RunAsClient
    public void testNotReady() {
        getReadiness()
            .assertResponseCodeDown()
            .assertMessagingStatusDown();
    }

    @Test
    @InSequence(2)
    @RunAsClient
    public void testPartialReadiness() {
        channelRegister.get(CHANNEL_CONNECTOR_IN).ready();
        channelRegister.get(CHANNEL_CONNECTOR_OUT).ready();
        channelRegister.get(CHANNEL_INNER).ready();
        // LIVE_EXCLUDED_CHANNEL is not excluded from readiness check
        getReadiness()
            .assertResponseCodeDown()
            .assertMessagingStatusDown();
    }

    @Test
    @InSequence(3)
    @RunAsClient
    public void testReadiness() {
        channelRegister.get(LIVE_EXCLUDED_CHANNEL).ready();
        getReadiness()
            .assertResponseCodeUp()
            .assertMessagingStatusUp();
    }

    @Test
    @InSequence(4)
    @RunAsClient
    public void testLiveness() {
        getLiveness()
            .assertResponseCodeUp()
            .assertMessagingStatusUp();
    }

    @Test
    @InSequence(5)
    @RunAsClient
    public void testLivenessWithFailedExcluded() {
        channelRegister.get(LIVE_EXCLUDED_CHANNEL).fail();
        // LIVE_EXCLUDED_CHANNEL is excluded from liveness check
        getLiveness()
            .assertResponseCodeUp()
            .assertMessagingStatusUp();
    }

    @Test
    @InSequence(6)
    @RunAsClient
    public void testOneErroredChannelLiveness() {
        channelRegister.get(CHANNEL_CONNECTOR_IN).fail();
        getLiveness()
            .assertResponseCodeDown()
            .assertMessagingStatusDown();
    }

    @Test
    @InSequence(7)
    @RunAsClient
    public void testOneErroredChannelReadiness() {
        getReadiness()
            .assertResponseCodeUp()
            .assertMessagingStatusUp();
    }

    @Test
    @InSequence(8)
    @RunAsClient
    public void testAllErroredChannelsLiveness() {
        channelRegister.get(CHANNEL_CONNECTOR_IN).fail();
        channelRegister.get(CHANNEL_CONNECTOR_OUT).fail();
        channelRegister.get(CHANNEL_INNER).fail();
        getLiveness()
            .assertResponseCodeDown()
            .assertMessagingStatusDown();
    }

    @Test
    @InSequence(9)
    @RunAsClient
    public void testAllErroredChannelsReadiness() {
        getReadiness()
            .assertResponseCodeUp()
            .assertMessagingStatusUp();
    }


}
