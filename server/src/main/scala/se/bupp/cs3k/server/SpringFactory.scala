package se.bupp.cs3k.server

import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-19
 * Time: 19:47
 * To change this template use File | Settings | File Templates.
 */
@Component
class SpringFactory {

  @Bean
  def objectMapper = new ObjectMapper()

}
