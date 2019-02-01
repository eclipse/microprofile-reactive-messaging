/*******************************************************************************
 * Copyright (c) 2018 Contributors to the Eclipse Foundation
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
 ******************************************************************************/
package org.eclipse.microprofile.reactive.messaging.tck.invalid;

import org.eclipse.microprofile.reactive.messaging.tck.ArchiveExtender;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.spi.DeploymentException;
import java.util.ServiceLoader;

@RunWith(Arquillian.class)
public class InvalidConfigurationTest {

  @Deployment(managed = false, name = "empty-incoming")
  public static Archive<JavaArchive> emptyIncoming() {
    JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
      .addClasses(BeanWithEmptyIncoming.class)
      .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

    ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(archive));
    return archive;
  }

  @Deployment(managed = false, name = "empty-outgoing")
  public static Archive<JavaArchive> emptyOutgoing() {
    JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
      .addClasses(BeanWithEmptyOutgoing.class)
      .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

    ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(archive));
    return archive;
  }

  @Deployment(managed = false, name = "invalid-publisher-method")
  public static Archive<JavaArchive> invalidPublisherMethod() {
    JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
      .addClasses(BeanWithBadOutgoingSignature.class)
      .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

    ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(archive));
    return archive;
  }

  @Deployment(managed = false, name = "incomplete-chain")
  public static Archive<JavaArchive> incompleteChain() {
    JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
      .addClasses(BeanWithBadOutgoingSignature.class)
      .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

    ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(archive));
    return archive;
  }

  @ArquillianResource
  private Deployer deployer;

  @Test(expected = DeploymentException.class)
  public void checkThatEmptyIncomingAreRejected() {
    deployer.deploy("empty-incoming");
  }

  @Test(expected = DeploymentException.class)
  public void checkThatEmptyOutgoingAreRejected() {
    deployer.deploy("empty-outgoing");
  }

  @Test(expected = DeploymentException.class)
  public void checkThatInvalidOutgoingSignaturesAreRejected() {
    deployer.deploy("invalid-publisher-method");
  }

  @Test(expected = DeploymentException.class)
  public void checkThatIncompleteChainsAreDetected() {
    deployer.deploy("incomplete-chain");
  }

}
