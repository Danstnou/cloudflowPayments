package payments.processor.settings

import cloudflow.streamlets.StringConfigParameter

trait MessageSettings {

  val ParticipantNotFoundMessageParameter: StringConfigParameter = StringConfigParameter(
    "participantNotFoundMessage",
    "Сообщение при неудаче поиска участника"
  )

  val SuccessfulPaymentMessageParameter: StringConfigParameter = StringConfigParameter(
    "successfulPaymentMessage",
    "Сообщение при успешном переводе"
  )
  val LackFundsMessageParameter: StringConfigParameter = StringConfigParameter(
    "lackFundsMessage",
    "Сообщение при нехватке средств"
  )

  val messageParameters = Vector(ParticipantNotFoundMessageParameter, SuccessfulPaymentMessageParameter, LackFundsMessageParameter)

}
