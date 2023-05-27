package org.di4j.di4j.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation is for marking a constructor in a service as the constructor<br>
 * to use when creating a instance of the service. This is useful when you have multiple<br>
 * constructors. DI4J will complain if you don't use this constructor then.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceProviderConstructor {
}
