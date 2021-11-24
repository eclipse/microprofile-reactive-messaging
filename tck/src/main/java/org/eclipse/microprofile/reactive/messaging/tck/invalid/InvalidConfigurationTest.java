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
package org.eclipse.microprofile.reactive.messaging.tck.invalid;

import org.eclipse.microprofile.reactive.messaging.tck.ArchiveExtender;
import org.eclipse.microprofile.reactive.messaging.tck.metrics.ConfigAsset;
import org.eclipse.microprofile.reactive.messaging.tck.metrics.TestConnector;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.ShouldThrowException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import jakarta.enterprise.inject.spi.DeploymentException;
import java.util.ServiceLoader;

@RunWith(Arquillian.class)
public class InvalidConfigurationTest {

    @Deployment(managed = false, name = "empty-incoming")
    @ShouldThrowException(value = DeploymentException.class, testable = true)
    public static Archive<JavaArchive> emptyIncoming() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
            .addClasses(BeanWithEmptyIncoming.class, ArchiveExtender.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(archive));
        return archive;
    }

    @Deployment(managed = false, name = "empty-outgoing")
    @ShouldThrowException(value = DeploymentException.class, testable = true)
    public static Archive<JavaArchive> emptyOutgoing() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
            .addClasses(BeanWithEmptyOutgoing.class, ArchiveExtender.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(archive));
        return archive;
    }

    @Deployment(managed = false, name = "invalid-publisher-method")
    @ShouldThrowException(value = DeploymentException.class, testable = true)
    public static Archive<JavaArchive> invalidPublisherMethod() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
            .addClasses(BeanWithBadOutgoingSignature.class, ArchiveExtender.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(archive));
        return archive;
    }

    @Deployment(managed = false, name = "incomplete-chain")
    @ShouldThrowException(value = DeploymentException.class, testable = true)
    public static Archive<JavaArchive> incompleteChain() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
            .addClasses(BeanWithIncompleteChain.class, ArchiveExtender.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(archive));
        return archive;
    }

    @Deployment(managed = false, name = "processor-missing-upstream")
    @ShouldThrowException(value = DeploymentException.class, testable = true)
    public static Archive<JavaArchive> processorMissingUpstream() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
            .addClasses(ProcessorMissingUpstream.class, ArchiveExtender.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(archive));
        return archive;
    }

    @Deployment(managed = false, name = "processor-missing-downstream")
    @ShouldThrowException(value = DeploymentException.class, testable = true)
    public static Archive<JavaArchive> processorMissingDownstream() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
            .addClasses(ProcessorMissingDownstream.class, ArchiveExtender.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(archive));
        return archive;
    }

    @Deployment(managed = false, name = "processor-multiple-upstreams")
    @ShouldThrowException(value = DeploymentException.class, testable = true)
    public static Archive<JavaArchive> processorMultipleUpstreams() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
            .addClasses(ProcessorMultipleUpstreams.class, ArchiveExtender.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(archive));
        return archive;
    }

    @Deployment(managed = false, name = "processor-multiple-downstreams")
    @ShouldThrowException(value = DeploymentException.class, testable = true)
    public static Archive<JavaArchive> processorMultipleDownstreams() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
            .addClasses(ProcessorMultipleDownstreams.class, ArchiveExtender.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(archive));
        return archive;
    }

    @Deployment(managed = false, name = "emitter-missing-downstream")
    @ShouldThrowException(value = DeploymentException.class, testable = true)
    public static Archive<JavaArchive> emitterMissingDownstream() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
            .addClasses(EmitterMissingDownstream.class, ArchiveExtender.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(archive));
        return archive;
    }

    @Deployment(managed = false, name = "emitter-multiple-downstreams")
    @ShouldThrowException(value = DeploymentException.class, testable = true)
    public static Archive<JavaArchive> emitterMultipleDownstreams() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
            .addClasses(EmitterMultipleDownstreams.class, ArchiveExtender.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(archive));
        return archive;
    }

    @Deployment(managed = false, name = "channel-missing-upstream")
    @ShouldThrowException(value = DeploymentException.class, testable = true)
    public static Archive<JavaArchive> channelMissingUpstream() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
            .addClasses(ChannelMissingUpstream.class, ArchiveExtender.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(archive));
        return archive;
    }

    @Deployment(managed = false, name = "channel-multiple-upstreams")
    @ShouldThrowException(value = DeploymentException.class, testable = true)
    public static Archive<JavaArchive> channelMultipleUpstreams() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
            .addClasses(ChannelMultipleUpstreams.class, ArchiveExtender.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(archive));
        return archive;
    }

    @Deployment(managed = false, name = "connector-missing-upstream")
    @ShouldThrowException(value = DeploymentException.class, testable = true)
    public static Archive<JavaArchive> connectorMissingUpstream() {
        ConfigAsset config = new ConfigAsset()
            .put("mp.messaging.outgoing.missing.connector", TestConnector.ID);

        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
            .addClasses(TestConnector.class, ArchiveExtender.class)
            .addAsResource(config, "META-INF/microprofile-config.properties")
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(archive));
        return archive;
    }

    @Deployment(managed = false, name = "connector-missing-downstream")
    @ShouldThrowException(value = DeploymentException.class, testable = true)
    public static Archive<JavaArchive> connectorMissingDownstream() {
        ConfigAsset config = new ConfigAsset()
            .put("mp.messaging.incoming.missing.connector", TestConnector.ID);

        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
            .addClasses(TestConnector.class, ArchiveExtender.class)
            .addAsResource(config, "META-INF/microprofile-config.properties")
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(archive));
        return archive;
    }

    @Deployment(managed = false, name = "connector-multiple-downstreams")
    @ShouldThrowException(value = DeploymentException.class, testable = true)
    public static Archive<JavaArchive> connectorMultipleDownstreams() {
        ConfigAsset config = new ConfigAsset()
            .put("mp.messaging.incoming.many.connector", TestConnector.ID);

        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
            .addClasses(TestConnector.class, BeanConsumingManyTwice.class, ArchiveExtender.class)
            .addAsResource(config, "META-INF/microprofile-config.properties")
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(archive));
        return archive;
    }

    @Deployment(managed = false, name = "connector-multiple-upstreams")
    @ShouldThrowException(value = DeploymentException.class, testable = true)
    public static Archive<JavaArchive> connectorMultipleUpstreams() {
        ConfigAsset config = new ConfigAsset()
            .put("mp.messaging.outgoing.many.connector", TestConnector.ID);

        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
            .addClasses(TestConnector.class, BeanProducingManyTwice.class, ArchiveExtender.class)
            .addAsResource(config, "META-INF/microprofile-config.properties")
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(archive));
        return archive;
    }

    @ArquillianResource
    private Deployer deployer;

    @Test
    public void checkThatEmptyIncomingAreRejected() {
        deployer.deploy("empty-incoming");
    }

    @Test
    public void checkThatEmptyOutgoingAreRejected() {
        deployer.deploy("empty-outgoing");
    }

    @Test
    public void checkThatInvalidOutgoingSignaturesAreRejected() {
        deployer.deploy("invalid-publisher-method");
    }

    @Test
    public void checkThatIncompleteChainsAreDetected() {
        deployer.deploy("incomplete-chain");
    }

    @Test
    public void checkThatProcessorsWithoutUpstreamAreDetected() {
        deployer.deploy("processor-missing-upstream");
    }

    @Test
    public void checkThatProcessorsWithoutDownstreamAreDetected() {
        deployer.deploy("processor-missing-downstream");
    }

    @Test
    public void checkThatProcessorsWithTooManyUpstreamsAreDetected() {
        deployer.deploy("processor-multiple-upstreams");
    }

    @Test
    public void checkThatProcessorsWithTooManyDownstreamsAreDetected() {
        deployer.deploy("processor-multiple-downstreams");
    }

    @Test
    public void checkThatEmitterWithoutDownstreamAreDetected() {
        deployer.deploy("emitter-missing-downstream");
    }

    @Test
    public void checkThatEmitterWithMultipleDownstreamsAreDetected() {
        deployer.deploy("emitter-multiple-downstreams");
    }

    @Test
    public void checkThatChannelWithoutUpstreamAreDetected() {
        deployer.deploy("channel-missing-upstream");
    }

    @Test
    public void checkThatChannelWithMultipleUpstreamsAreDetected() {
        deployer.deploy("channel-multiple-upstreams");
    }

    @Test
    public void checkThatIncomingConnectorWithoutDownstreamAreDetected() {
        deployer.deploy("connector-missing-downstream");
    }

    @Test
    public void checkThatIncomingConnectorWithMultipleDownstreamAreDetected() {
        deployer.deploy("connector-multiple-downstreams");
    }

    @Test
    public void checkThatOutgoingConnectorWithoutUpstreamAreDetected() {
        deployer.deploy("connector-missing-upstream");
    }

    @Test
    public void checkThatOutgoingConnectorWithMultipleUpstreamsAreDetected() {
        deployer.deploy("connector-multiple-upstreams");
    }

}
