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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import static org.eclipse.microprofile.reactive.messaging.tck.health.HealthBase.LIVE_EXCLUDED_CHANNEL;

@ApplicationScoped
public class HealthLiveExcludedTestBean {

    @Inject
    private ChannelRegister channelRegisterBean;


    @Incoming(LIVE_EXCLUDED_CHANNEL)
    public Subscriber<String> consumeInnerChannel() {
        return channelRegisterBean.<String>get(LIVE_EXCLUDED_CHANNEL).subscriber();
    }

    @Outgoing(LIVE_EXCLUDED_CHANNEL)
    public Publisher<String> produceInnerChannel() {
        return channelRegisterBean.<String>get(LIVE_EXCLUDED_CHANNEL).publisher();
    }

}
