/**
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
package org.eclipse.microprofile.reactive.messaging.tck.connector;

import org.eclipse.microprofile.reactive.messaging.tck.ArchiveExtender;
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

/**
 * This test verifies that the deployment fails if the connector implementation is unknown.
 */
@RunWith(Arquillian.class)
public class MissingConnectorTest {

    @ArquillianResource
    private Deployer deployer;

    @Deployment(managed = false, name = "missing-connector")
    @ShouldThrowException(value = DeploymentException.class, testable = true)
    public static Archive<JavaArchive> missingConnectorDeployment() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
            .addClasses(MyProcessor.class, ArchiveExtender.class)
            .addAsManifestResource(MissingConnectorTest.class.getResource("connector-config.properties"), "microprofile-config.properties")
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(archive));
        return archive;
    }

    @Test
    public void testWhenTheConnectorAreNotConfigured() {
        deployer.deploy("missing-connector");
    }

    @Deployment(managed = false, name = "missing-stream")
    @ShouldThrowException(value = DeploymentException.class, testable = true)
    public static Archive<JavaArchive> missingStreamDeployment() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
            .addClasses(MyProcessorWithBadStreamName.class, DummyConnector.class, ArchiveExtender.class)
            .addAsManifestResource(MissingConnectorTest.class.getResource("connector-config.properties"), "microprofile-config.properties")
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(archive));
        return archive;
    }

    @Test
    public void testWhenTheStreamNameDoesNotMatch() {
        deployer.deploy("missing-stream");
    }

}
