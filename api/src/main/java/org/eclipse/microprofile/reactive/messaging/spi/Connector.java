package org.eclipse.microprofile.reactive.messaging.spi;


import org.eclipse.microprofile.reactive.messaging.MessagingProvider;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Qualifier used on connector implementations to indicate the associated {@link MessagingProvider}.
 * <p>
 * The value indicates  the {@link MessagingProvider} class associated with  the bean implementing either
 * {@link IncomingConnectorFactory} or {@link OutgoingConnectorFactory} or both.
 * <p>
 * Note that the {@link MessagingProvider} is a user-facing interface used in the configuration.
 */
@Qualifier
@Retention(RUNTIME)
@Target(TYPE)
public @interface Connector {

    /**
     * @return the {@link MessagingProvider} associated with the bean implementing {@link IncomingConnectorFactory}
     * or {@link OutgoingConnectorFactory}. Must not be {@code null}. Returning {@code null} will cause a deployment
     * failure.
     */
    Class<? extends MessagingProvider> value();

}
