/*
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
package org.eclipse.microprofile.reactive.messaging.tck;

import java.util.ServiceLoader;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public abstract class TckBase {

    public final static Executor EXECUTOR = Executors.newSingleThreadExecutor();

    public static JavaArchive getBaseArchive() {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class)
                .addClasses(ArchiveExtender.class, TckBase.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

        ServiceLoader.load(ArchiveExtender.class).iterator().forEachRemaining(ext -> ext.extend(archive));

        return archive;
    }

}
