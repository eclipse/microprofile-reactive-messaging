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
package org.eclipse.microprofile.reactive.messaging.tck.signatures.invalid;

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

import javax.enterprise.inject.spi.DefinitionException;
import java.util.ServiceLoader;

@RunWith(Arquillian.class)
public class InvalidSubscriberSignatureTest {

    @ArquillianResource
    private Deployer deployer;

    @Deployment(managed = false, name = "incoming-returning-object")
    @ShouldThrowException(value = DefinitionException.class, testable = true)
    public static Archive<JavaArchive> incomingReturningNonVoid() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
            .addClasses(IncomingReturningNonVoid.class, ArchiveExtender.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(archive));
        return archive;
    }

    @Test
    public void checkThatIncomingShouldNotReturnObject() {
        deployer.deploy("incoming-returning-object");
    }

    @Deployment(managed = false, name = "incoming-returning-non-void-cs")
    @ShouldThrowException(value = DefinitionException.class, testable = true)
    public static Archive<JavaArchive> incomingReturningNonVoidCompletionStage() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
            .addClasses(IncomingReturningNonVoidCompletionStage.class, ArchiveExtender.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(archive));
        return archive;
    }

    @Test
    public void checkThatIncomingShouldNotReturnNonVoidCompletionStage() {
        deployer.deploy("incoming-returning-non-void-cs");
    }

}
