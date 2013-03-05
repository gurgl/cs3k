package se.bupp.cs3k.server.web.auth;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface AnonymousOnly {
  
}
