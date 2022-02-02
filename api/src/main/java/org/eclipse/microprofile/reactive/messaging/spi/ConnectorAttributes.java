/*
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
package org.eclipse.microprofile.reactive.messaging.spi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This class is used to allow multiple {@link ConnectorAttribute} declarations.
 *
 * This class should not be used directly. Instead, multiple {@link ConnectorAttribute} should be used on the class
 * annotated with {@link Connector}.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface ConnectorAttributes {

    /**
     * @return the array of {@link ConnectorAttribute}, must not contain {@code null}.
     */
    ConnectorAttribute[] value();

}
