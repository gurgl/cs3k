package se.bupp.cs3k.server.web.generic

import org.apache.wicket.model.IModel

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2013-02-25
 * Time: 00:27
 * To change this template use File | Settings | File Templates.
 */
object Fodel {

  type SetGet[T] = (() ⇒ T, T ⇒ Unit)

  type PropSetGet[M,T] = ((M) ⇒ T, (M,T) ⇒ Unit)

}

@SerialVersionUID(1L)
class Fodel[T](val getter:() ⇒ T, val setter:(T) ⇒ Unit) extends IModel[T] with Serializable {

  def this(getter:() ⇒ T) = this(getter, null)

  /**
   * Constructs a fodel with only a setter, which also returns the backing object, so can be used as a setter
   */
  //def this(setter:(T) => T) = this(null, setter)

  /**
   * Executes the embedded getter function #getter, to return the backing object.
   */
  override def getObject:T = getter()

  /**
   * Executes the embedded getter function #setter, to assign the backing object.
   *
   * @throws UnsupportedOperationException if the Fodel is read-only ( has no setter functionn).
   */
  override def setObject(value:T):Unit = {
    if(setter==null)
      throw new UnsupportedOperationException( "You cannot set the object on a readonly model.")
    setter(value)
  }

  def detach = ()

}

/*
@SerialVersionUID(1L)
class PFodel[T,M](obj:M, getter:(M) ⇒ T, setter:(M,T) ⇒ Unit) extends IModel[T] with Serializable {

  def this(getter:(M) ⇒ T) = this(getter, null)

  /**
   * Constructs a fodel with only a setter, which also returns the backing object, so can be used as a setter
   */
  //def this(setter:(T) => T) = this(null, setter)

  /**
   * Executes the embedded getter function #getter, to return the backing object.
   */
  override def getObject:T = getter(obj)

  /**
   * Executes the embedded getter function #setter, to assign the backing object.
   *
   * @throws UnsupportedOperationException if the Fodel is read-only ( has no setter functionn).
   */
  override def setObject(value:T):Unit = {
    if(setter==null)
      throw new UnsupportedOperationException( "You cannot set the object on a readonly model.")
    setter(obj,value)
  }

  def detach = ()

}

*/
/**
 * Basic extension to the Fodel that uses strings.
 *
 * @author Antony Stubbs
 */
class FodelString(getter:() ⇒ String, setter:(String) ⇒ Unit) extends Fodel[String](getter, setter) {
  def this(getter:() ⇒ String) = this(getter, null)
}