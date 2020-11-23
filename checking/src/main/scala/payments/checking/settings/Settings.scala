package payments.checking.settings

import cloudflow.streamlets.StringConfigParameter

trait Settings {

  val MaskPaymentParameter: StringConfigParameter = StringConfigParameter(
    "maskPayment",
    "Маска платежа"
  )
  val IncorrectPaymentMessageParameter: StringConfigParameter = StringConfigParameter(
    "incorrectPaymentMessage",
    "Сообщение при некорректном платеже"
  )

  val settings = Vector(MaskPaymentParameter, IncorrectPaymentMessageParameter)

}
