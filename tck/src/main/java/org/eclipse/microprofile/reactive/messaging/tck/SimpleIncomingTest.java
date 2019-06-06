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
package org.eclipse.microprofile.reactive.messaging.tck;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.ServiceLoader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@RunWith(Arquillian.class)
public class SimpleIncomingTest {

  @Deployment
  public static Archive<JavaArchive> deployment() {
    JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
      .addClasses(SimpleIncomingBean.class, ValueCollector.class, StringSource.class)
      .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

    ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(archive));
    return archive;
  }

  @Inject
  private SimpleIncomingBean simple;

  @Test
  public void testReceptionWithValues() {
      await().until(() -> simple.getValues().size() == StringSource.VALUES.size());
      assertThat(simple.getValues()).containsExactlyElementsOf(StringSource.VALUES);
  }


}
