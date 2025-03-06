package model

import db.entities.Item
import db.tables.ItemRepository

import scala.concurrent.{ExecutionContext, Future}

case class ItemDTO(name: String)

object ItemDTO {
  def getItem(dto: ItemDTO, repository: ItemRepository)(implicit ec: ExecutionContext): Future[Option[Item]] = {
    repository.findByName(dto.name)
  }
}
