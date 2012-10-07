package se.bupp.cs3k.server

import javax.persistence.{GenerationType, GeneratedValue, Id, Entity}
/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-06
 * Time: 13:09
 * To change this template use File | Settings | File Templates.
 */
/*@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
abstract class Person {
 */

@Entity
class ApiPlayer(var s:String) {
  @Id @GeneratedValue(strategy=GenerationType.AUTO) var id:Int = _

  def this() = this("")
  override def toString = id + " " + s
}
