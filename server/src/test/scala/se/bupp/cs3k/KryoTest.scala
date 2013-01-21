package se.bupp.cs3k

import org.specs2.mutable.Specification

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.serializers.BeanSerializer
import com.esotericsoftware.kryo.io.{Input, Output}
import java.io.ByteArrayOutputStream
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-02
 * Time: 19:18
 * To change this template use File | Settings | File Templates.
 */
//@RunWith(classOf[JUnitRunner])
class KryoTest extends Specification {

  "should return my address" should {
    "handle conversions1" in {



      var kryo: Kryo = new Kryo
      kryo.setDefaultSerializer(classOf[BeanSerializer[_]])
      var response: LobbyJoinResponse = new LobbyJoinResponse(123)


      if (System.getSecurityManager() == null) {
        System.setSecurityManager(new SecurityManager());
      }
      var stream: ByteArrayOutputStream = new ByteArrayOutputStream(1024)


      //System.setSecurityManager(null);

      var output: Output = new Output(stream)
      kryo.writeClassAndObject(output,response)

      output.flush();
      var andObject: AnyRef = kryo.readClassAndObject(new Input(stream.toByteArray))


      response.shouldEqual(andObject)

    }

  }
}
