package se.bupp.cs3k.server

import org.springframework.transaction.{TransactionStatus, PlatformTransactionManager}
import org.springframework.transaction.support.{TransactionCallback, TransactionCallbackWithoutResult, TransactionTemplate}

/**
 * Created with IntelliJ IDEA.
 * User: karlw
 * Date: 2012-10-29
 * Time: 01:54
 * To change this template use File | Settings | File Templates.
 */




object Util {

  class Validation[A,B](val e:Either[A,B]) {
    def onSuccess[C](f:(B)=> Either[A,C]) = e.right.flatMap( x => f(x))
  }
  implicit def eitherSuccess[A,B](e:Either[A,B]) = new Validation(e)

  def _hashCode(x: Product): Int = {
    val arr =  x.productArity
    var code = arr
    var i = 0
    while (i < arr) {
      val elem = x.productElement(i)
      code = code * 41 + (if (elem == null) 0 else elem.hashCode())
      i += 1
    }
    code
  }

  def inTx[G](txMgr:PlatformTransactionManager)(body : => G) : G = {
    new TransactionTemplate(txMgr).execute(new TransactionCallback[G] {
      def doInTransaction(p1: TransactionStatus) = {
        body
      }
    })
  }
}
