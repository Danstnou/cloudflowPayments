/** MACHINE-GENERATED FROM AVRO SCHEMA. DO NOT EDIT DIRECTLY */
package payments.datamodel

import scala.annotation.switch

/**
 * @param from идентификатор инициатора платежа
 * @param to идентификатор принимающей стороны
 * @param value сумма перевода
 */
case class Payment(var from: String, var to: String, var value: Long) extends org.apache.avro.specific.SpecificRecordBase {
  def this() = this("", "", 0L)
  def get(field$: Int): AnyRef = {
    (field$: @switch) match {
      case 0 => {
        from
      }.asInstanceOf[AnyRef]
      case 1 => {
        to
      }.asInstanceOf[AnyRef]
      case 2 => {
        value
      }.asInstanceOf[AnyRef]
      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
    }
  }
  def put(field$: Int, value: Any): Unit = {
    (field$: @switch) match {
      case 0 => this.from = {
        value.toString
      }.asInstanceOf[String]
      case 1 => this.to = {
        value.toString
      }.asInstanceOf[String]
      case 2 => this.value = {
        value
      }.asInstanceOf[Long]
      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
    }
    ()
  }
  def getSchema: org.apache.avro.Schema = Payment.SCHEMA$
}

object Payment {
  val SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Payment\",\"namespace\":\"payments.datamodel\",\"fields\":[{\"name\":\"from\",\"type\":\"string\",\"doc\":\"идентификатор инициатора платежа\"},{\"name\":\"to\",\"type\":\"string\",\"doc\":\"идентификатор принимающей стороны\"},{\"name\":\"value\",\"type\":\"long\",\"doc\":\"сумма перевода\"}]}")
}