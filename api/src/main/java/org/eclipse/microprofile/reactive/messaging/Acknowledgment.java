/**
 * Copyright (c) 2018, 2019 Contributors to the Eclipse Foundation
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
package org.eclipse.microprofile.reactive.messaging;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Configure the acknowledgement policy for the given {@code @Incoming}.
 *
 * Reactive Messaging proposes four acknowledgement strategies:
 * <ul>
 * <li><code>MANUAL</code>: the acknowledgement (positive or negative) is up to the user. This is the default strategy
 * for methods ingesting or producing {@link Message}.</li>
 * <li><code>POST_PROCESSING</code>: acknowledges the incoming message once the produced message is acknowledged. This
 * is the default strategy for methods ingesting or producing single payloads.</li>
 * <li><code>PRE_PROCESSING</code>: acknowledges the incoming messages before calling the method.</li>
 * <li><code>NONE</code>: do not apply any acknowledgement.</li>
 * </ul>
 *
 * The set of supported acknowledgment policies depends on the method signature. The following list gives the supported
 * strategies for some common use cases.
 *
 * <ul>
 * <li><code> @Incoming("channel") void method(I payload)</code>: Post-processing (default), Pre-processing, None</li>
 * <li><code> @Incoming("channel") CompletionStage&lt;?&gt; method(I payload)</code>: Post-processing (default),
 * Pre-processing, None</li>
 * <li><code> @Incoming("in") @Outgoing("out") Message&lt;O&gt; method(Message&lt;I&gt; msg)</code>: , Manual (default),
 * Pre-processing, None</li>
 * <li><code> @Incoming("in") @Outgoing("out") O method(I payload)</code>: Post-Processing (default), Pre-processing,
 * None</li>
 * </ul>
 *
 * Note that all messages must be acknowledged. An absence of acknowledgment is considered as a failure.
 *
 * The following table lists the supported strategies (and the default) for each supported signature:
 *
 * <table style="border-collapse: collapse" cellpadding="3" cellspacing="0">
 * <thead>
 * <tr>
 * <th style="border: 1px solid #999; padding: 0.7rem; text-align: left;"><strong>Signature</strong></th>
 * <th style="border: 1px solid #999; padding: 0.7rem; text-align: left;"><strong>Default Acknowledgement
 * Strategy</strong></th>
 * <th style="border: 1px solid #999; padding: 0.7rem; text-align: left;"><strong>Supported Strategies</strong></th>
 * </tr>
 * </thead> <tbody>
 * <tr>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;"><div class="content"><div class=
 * "listingblock"> <div class="content">
 * 
 * <pre>
 * <code>@Incoming("channel")
 * Subscriber&lt;Message&lt;I&gt;&gt; method()</code>
 * </pre>
 * 
 * </div> </div></div></td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * Manual
 * </p>
 * </td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * None, Pre-Processing, Post-Processing (when the <code>onNext</code> method returns), Manual
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;"><div class="content"><div class=
 * "listingblock"> <div class="content">
 * 
 * <pre>
 * <code>@Incoming("channel")
 * Subscriber&lt;I&gt; method()</code>
 * </pre>
 * 
 * </div> </div></div></td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * Post-Processing
 * </p>
 * </td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * None, Pre-Processing, Post-Processing (when the <code>onNext</code> method returns)
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;"><div class="content"><div class=
 * "listingblock"> <div class="content">
 * 
 * <pre>
 * <code>@Incoming("channel")
 * SubscriberBuilder&lt;Message&lt;I&gt;, <span style="color:#0a8;font-weight:bold">Void</span>&gt; method()</code>
 * </pre>
 * 
 * </div> </div></div></td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * Manual
 * </p>
 * </td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * None, Pre-Processing, Post-Processing (when the <code>onNext</code> method returns), Manual
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;"><div class="content"><div class=
 * "listingblock"> <div class="content">
 * 
 * <pre>
 * <code>@Incoming("channel")
 * SubscriberBuilder&lt;I, <span style="color:#0a8;font-weight:bold">Void</span>&gt; method()</code>
 * </pre>
 * 
 * </div> </div></div></td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * Post-Processing
 * </p>
 * </td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * None, Pre-Processing, Post-Processing (when the <code>onNext</code> method returns)
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;"><div class="content"><div class=
 * "listingblock"> <div class="content">
 * 
 * <pre>
 * <code>@Incoming("channel")
 * <span style="color:#339;font-weight:bold">void</span> method(I payload)</code>
 * </pre>
 * 
 * </div> </div></div></td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * Post-Processing
 * </p>
 * </td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * None, Pre-Processing, Post-Processing (when the method returns)
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;"><div class="content"><div class=
 * "listingblock"> <div class="content">
 * 
 * <pre>
 * <code>@Incoming("channel")
 * CompletionStage&lt;?&gt; method(Message&lt;I&gt; msg)</code>
 * </pre>
 * 
 * </div> </div></div></td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * Manual
 * </p>
 * </td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * None, Pre-Processing, Post-Processing (when the returned <code>CompletionStage</code> is completed), Manual
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;"><div class="content"><div class=
 * "listingblock"> <div class="content">
 * 
 * <pre>
 * <code>@Incoming("channel")
 * CompletionStage&lt;?&gt; method(I payload)</code>
 * </pre>
 * 
 * </div> </div></div></td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * Post-Processing
 * </p>
 * </td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * None, Pre-Processing, Post-Processing (when the returned <code>CompletionStage</code> is completed)
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;"><div class="content"><div class=
 * "listingblock"> <div class="content">
 * 
 * <pre>
 * <code>@Incoming("in")
 * <span style="color:#007">@Outgoing("out")
 * Processor&lt;Message&lt;I&gt;, Message&lt;O&gt;&gt; method()</code>
 * </pre>
 * 
 * </div> </div></div></td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * Manual
 * </p>
 * </td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * None, Pre-Processing, Manual
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;"><div class="content"><div class=
 * "listingblock"> <div class="content">
 * 
 * <pre>
 * <code>@Incoming("in")
 * <span style="color:#007">@Outgoing("out")
 * Processor&lt;I, O&gt; method();</code>
 * </pre>
 * 
 * </div> </div></div></td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * Pre-Processing
 * </p>
 * </td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * None, Pre-Processing Post-Processing can be optionally supported by implementations, however it requires a 1:1
 * mapping between the incoming element and the outgoing element.
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;"><div class="content"><div class=
 * "listingblock"> <div class="content">
 * 
 * <pre>
 * <code>@Incoming("in")
 * <span style="color:#007">@Outgoing("out")
 * ProcessorBuilder&lt;Message&lt;I&gt;, Message&lt;O&gt;&gt; method();</code>
 * </pre>
 * 
 * </div> </div></div></td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * Manual
 * </p>
 * </td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * None, Pre-Processing, Manual
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;"><div class="content"><div class=
 * "listingblock"> <div class="content">
 * 
 * <pre>
 * <code>@Incoming("in")
 * <span style="color:#007">@Outgoing("out")
 * ProcessorBuilder&lt;I, O&gt; method();</code>
 * </pre>
 * 
 * </div> </div></div></td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * Pre-Processing
 * </p>
 * </td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * None, Pre-Processing Post-Processing can be optionally supported by implementations, however it requires a 1:1
 * mapping the incoming element and the outgoing element.
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;"><div class="content"><div class=
 * "listingblock"> <div class="content">
 * 
 * <pre>
 * <code>@Incoming("in")
 * <span style="color:#007">@Outgoing("out")
 * Publisher&lt;Message&lt;O&gt;&gt; method(Message&lt;I&gt; msg)</code>
 * </pre>
 * 
 * </div> </div></div></td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * Manual
 * </p>
 * </td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * None, Manual, Pre-Processing
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;"><div class="content"><div class=
 * "listingblock"> <div class="content">
 * 
 * <pre>
 * <code>@Incoming("in")
 * <span style="color:#007">@Outgoing("out")
 * Publisher&lt;O&gt; method(I payload)</code>
 * </pre>
 * 
 * </div> </div></div></td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * Pre-Processing
 * </p>
 * </td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * None, Pre-Processing
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;"><div class="content"><div class=
 * "listingblock"> <div class="content">
 * 
 * <pre>
 * <code>@Incoming("in")
 * <span style="color:#007">@Outgoing("out")
 * PublisherBuilder&lt;Message&lt;O&gt;&gt; method(Message&lt;I&gt; msg)</code>
 * </pre>
 * 
 * </div> </div></div></td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * Manual
 * </p>
 * </td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * None, Manual, Pre-Processing
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;"><div class="content"><div class=
 * "listingblock"> <div class="content">
 * 
 * <pre>
 * <code>@Incoming("in")
 * <span style="color:#007">@Outgoing("out")
 * PublisherBuilder&lt;O&gt; method(I payload)</code>
 * </pre>
 * 
 * </div> </div></div></td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * Pre-Processing
 * </p>
 * </td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * None, Pre-Processing
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;"><div class="content"><div class=
 * "listingblock"> <div class="content">
 * 
 * <pre>
 * <code>@Incoming("in")
 * <span style="color:#007">@Outgoing("out")
 * Message&lt;O&gt; method(Message&lt;I&gt; msg)</code>
 * </pre>
 * 
 * </div> </div></div></td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * Manual
 * </p>
 * </td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * None, Manual, Pre-Processing
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;"><div class="content"><div class=
 * "listingblock"> <div class="content">
 * 
 * <pre>
 * <code>@Incoming("in")
 * <span style="color:#007">@Outgoing("out")
 * O method(I payload)</code>
 * </pre>
 * 
 * </div> </div></div></td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * Post-Processing
 * </p>
 * </td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * None, Pre-Processing, Post-Processing (when the message wrapping the produced payload is acknowledged)
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;"><div class="content"><div class=
 * "listingblock"> <div class="content">
 * 
 * <pre>
 * <code>@Incoming("in")
 * <span style="color:#007">@Outgoing("out")
 * CompletionStage&lt;Message&lt;O&gt;&gt; method(Message&lt;I&gt; msg)</code>
 * </pre>
 * 
 * </div> </div></div></td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * Manual
 * </p>
 * </td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * None, Manual, Pre-Processing
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;"><div class="content"><div class=
 * "listingblock"> <div class="content">
 * 
 * <pre>
 * <code>@Incoming("in")
 * <span style="color:#007">@Outgoing("out")
 * CompletionStage&lt;O&gt; method(I payload)</code>
 * </pre>
 * 
 * </div> </div></div></td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * Post-Processing
 * </p>
 * </td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * None, Pre-Processing, Post-Processing (when the message wrapping the produced payload is acknowledged)
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;"><div class="content"><div class=
 * "listingblock"> <div class="content">
 * 
 * <pre>
 * <code>@Incoming("in")
 * <span style="color:#007">@Outgoing("out")
 * Publisher&lt;Message&lt;O&gt;&gt; method(Publisher&lt;Message&lt;I&gt;&gt; pub)</code>
 * </pre>
 * 
 * </div> </div></div></td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * Manual
 * </p>
 * </td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * None, Manual, Pre-Processing
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;"><div class="content"><div class=
 * "listingblock"> <div class="content">
 * 
 * <pre>
 * <code>@Incoming("in")
 * <span style="color:#007">@Outgoing("out")
 * PublisherBuilder&lt;Message&lt;O&gt;&gt; method(PublisherBuilder&lt;Message&lt;I&gt;&gt; pub)</code>
 * </pre>
 * 
 * </div> </div></div></td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * Manual
 * </p>
 * </td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * None, Manual, Pre-Processing
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;"><div class="content"><div class=
 * "listingblock"> <div class="content">
 * 
 * <pre>
 * <code>@Incoming("in")
 * <span style="color:#007">@Outgoing("out")
 * Publisher&lt;O&gt; method(Publisher&lt;I&gt; pub)</code>
 * </pre>
 * 
 * </div> </div></div></td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * Pre-Processing
 * </p>
 * </td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * None, Pre-Processing
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;"><div class="content"><div class=
 * "listingblock"> <div class="content">
 * 
 * <pre>
 * <code>@Incoming("in")
 * <span style="color:#007">@Outgoing("out")
 * PublisherBuilder&lt;O&gt; method(PublisherBuilder&lt;I&gt; pub)</code>
 * </pre>
 * 
 * </div> </div></div></td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * Pre-Processing
 * </p>
 * </td>
 * <td style="border: 1px solid #999; padding: 0.5rem; text-align: left;">
 * <p>
 * None, Pre-Processing
 * </p>
 * </td>
 * </tr>
 * </tbody>
 * </table>
 *
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Acknowledgment {

    enum Strategy {

        /**
         * Acknowledgment managed by the user code. No automatic acknowledgment is performed. This strategy is only
         * supported by methods consuming {@link Message} instances.
         */
        MANUAL,

        /**
         * Acknowledgment performed automatically before the processing of the message by the user code.
         */
        PRE_PROCESSING,

        /**
         * Acknowledgment performed automatically once the message has been processed. When {@code POST_PROCESSING} is
         * used, the incoming message is acknowledged when the produced message is acknowledged.
         *
         * Notice that this mode is not supported for all signatures. When supported, it's the default policy.
         *
         */
        POST_PROCESSING,

        /**
         * No acknowledgment is performed, neither implicitly or explicitly. It means that the incoming messages are
         * going to be acknowledged in a different location or using a different mechanism.
         */
        NONE
    }

    /**
     * @return the acknowledgement policy.
     */
    Strategy value();

}
