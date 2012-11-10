package se.bupp.cs3k.server.util

import org.hibernate.usertype.UserType
import java.sql.{PreparedStatement, ResultSet, Types}

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-11-10
 * Time: 12:43
 * To change this template use File | Settings | File Templates.
 */
/*
trait Enumv  {

  this: Enumeration =>

  private var nameDescriptionMap =
    scala.collection.mutable.Map[String, String]()

  def Value(name: String, desc: String) : Value = {
    nameDescriptionMap += (name -> desc)
    new Val(name)
  }

  def getDescriptionOrName(ev: this.Value) = {
    try {
      nameDescriptionMap(""+ev)
    } catch {
      case e: NoSuchElementException => ev.toString
    }
  }

  def getNameDescriptionList =  this.elements.toList.map(
    v:Any => (v.toString, getDescriptionOrName(v) ) ).toList
}



abstract class EnumvType(val et: Enumeration with Enumv)
  extends UserType {

  val SQL_TYPES = Array({Types.VARCHAR})

  override def sqlTypes() = SQL_TYPES

  override def returnedClass = classOf[et.Value]

  override def equals(x: Object, y: Object): Boolean = {
    return x == y
  }

  override def hashCode(x: Object) = x.hashCode

  override def nullSafeGet(resultSet: ResultSet,
                           names: Array[String],
                           owner: Object): Object = {
    val value = resultSet.getString(names(0))
    if (resultSet.wasNull()) {
      null
    } else {
      et.valueOf(value).getOrElse(null)
    }
  }

  override def nullSafeSet(statement: PreparedStatement,
                           value: Object,
                           index: Int): Unit = {
    if (value == null) {
      statement.setNull(index, Types.VARCHAR)
    } else {
      val en = value.toString
      statement.setString(index, en)
    }
  }

  override def deepCopy(value: Object): Object = value

  override def isMutable() = false

  override def disassemble(value: Object) =
    value.asInstanceOf[Serializable]

  override def assemble(cached: Serializable, owner: Object):
  Serializable = cached

  override def replace(original: Object,
                       target: Object,
                       owner: Object) = original

}
*/