package payments.ingestor.settings

import cloudflow.streamlets.StringConfigParameter

trait Settings {

  val CatalogParameter = StringConfigParameter(
    "catalog",
    "Каталог, откуда будут вычитываться файлы с платежами"
  )
  val MaskFileParameter = StringConfigParameter(
    "maskFile",
    "Маска файлов в которых хранятся платежы"
  )

  val settings = Vector(CatalogParameter, MaskFileParameter)

}
