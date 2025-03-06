package db.tables

import db.{DatabaseConfig, ProductionDatabaseConfig}
import db.entities.Item
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class ItemTable(tag: Tag) extends Table[Item](tag, "items"){
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def unit = column[String]("unit")
  def price = column[BigDecimal]("price")

  override def * = (id.?, name, unit, price) <> (Item.tupled, Item.unapply)
}

class ItemRepository(dbConfig: DatabaseConfig = ProductionDatabaseConfig)(implicit ec: ExecutionContext) {
  val items = TableQuery[ItemTable]
  val db = dbConfig.db

  def createTable(): Future[Unit] = {
    db.run(items.schema.createIfNotExists)
  }

  def insert(item: Item): Future[Int] = {
    db.run(items += item)
  }

  def fetchItems(): Future[Seq[Item]] = {
    db.run(items.result)
  }

  def findByName(name: String): Future[Option[Item]] = {
    db.run(items.filter(_.name === name).result.headOption)
  }

  def dropTable(): Future[Unit] = {
    db.run(items.schema.drop)
  }
}

object ItemRepository {
  def apply()(implicit ec: ExecutionContext): ItemRepository = new ItemRepository()
}