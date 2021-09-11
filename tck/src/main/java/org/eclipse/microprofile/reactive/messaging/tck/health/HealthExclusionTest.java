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

/**
 * Channel excluded from readiness and liveness check scenario:
 * <ul>
 * <li>1. Assert {@code /health/ready} is DOWN as onSubscribe has not been issued on any channel yet</li>
 * <li>2. Send onSubscribe signal all channels except the excluded one</li>
 * <li>3. Assert {@code /health/ready} is UP as onSubscribe has been sent to all channels except the excluded one</li>
 * <li>4. Assert {@code /health/live} is UP as not onError nor cancel signal has been issued on any channel</li>
 * <li>5. Send onSubscribe signal to excluded channel</li>
 * <li>6. Send onError signal to excluded channel</li>
 * <li>7. Assert {@code /health/live} is UP as onError signal has been issued only on excluded channel</li>
 * <li>8. Send onError signal to one of the not excluded channels</li>
 * <li>9. Assert {@code /health/live} is DOWN as onError signal has been issued</li>
 * <li>10. Assert {@code /health/ready} is still UP</li>
 * <li>11. Send onError signal to the remaining channels</li>
 * <li>12. Assert {@code /health/live} is still DOWN</li>
 * <li>13. Assert {@code /health/ready} is still UP</li>
 * </ul>
 */
@RunWith(Arquillian.class)
public class HealthExclusionTest extends HealthBase{

    @Inject
    private ChannelRegister channelRegister;

    @Deployment
    public static WebArchive deployment() {
        return prepareArchive(HealthAllExcludedTestBean.class);
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
    public void testReadiness() {
        channelRegister.get(CHANNEL_CONNECTOR_IN).ready();
        channelRegister.get(CHANNEL_CONNECTOR_OUT).ready();
        channelRegister.get(CHANNEL_INNER).ready();
        // ALL_EXCLUDED_CHANNEL is excluded by config
        getReadiness()
            .assertResponseCodeUp()
            .assertMessagingStatusUp();
    }

    @Test
    @InSequence(3)
    @RunAsClient
    public void testLiveness() {
        getLiveness()
            .assertResponseCodeUp()
            .assertMessagingStatusUp();
    }

    @Test
    @InSequence(4)
    @RunAsClient
    public void testLivenessWithFailedExcluded() {
        channelRegister.get(ALL_EXCLUDED_CHANNEL).ready();
        channelRegister.get(ALL_EXCLUDED_CHANNEL).fail();
        getLiveness()
            .assertResponseCodeUp()
            .assertMessagingStatusUp();
    }

    @Test
    @InSequence(5)
    @RunAsClient
    public void testOneErroredChannelLiveness() {
        channelRegister.get(CHANNEL_CONNECTOR_IN).fail();
        getLiveness()
            .assertResponseCodeDown()
            .assertMessagingStatusDown();
    }

    @Test
    @InSequence(6)
    @RunAsClient
    public void testOneErroredChannelReadiness() {
        getReadiness()
            .assertResponseCodeUp()
            .assertMessagingStatusUp();
    }

    @Test
    @InSequence(7)
    @RunAsClient
    public void testAllErroredChannelsLiveness() {
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
