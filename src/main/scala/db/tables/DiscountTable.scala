package db.tables

import db.{DatabaseConfig, ProductionDatabaseConfig}
import db.entities.Discount
import slick.jdbc.SQLiteProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class DiscountTable(tag: Tag) extends Table[Discount](tag, "discounts"){
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def description = column[String]("description")
  def startTime = column[Option[Long]]("valid_from")
  def endTime = column[Option[Long]]("valid_until")
  def amount = column[Option[BigDecimal]]("amount")
  def percentage = column[Option[BigDecimal]]("percentage")
  def for_item = column[Option[Int]]("for_item")
  def depends_on_items = column[String]("depends_on_items")

  def * =
    (id.?, name, description, startTime, endTime, amount, percentage, for_item, depends_on_items) <>
      (
        {case (id, name, description, startTime, endTime, amount, percentage, for_item, depends_on_items_json) =>
          Discount(id, name, description, startTime, endTime, amount.filter(a => a != null), percentage.filter(p => p != null), for_item, Discount.dependsOnItemsDecode(depends_on_items_json))
        },
        (discount: Discount) => Some(
          (discount.id, discount.name, discount.description, discount.startTime, discount.endTime, discount.amount, discount.percentage, discount.forItem, Discount.dependsOnItemsEncode(discount.dependsOnItems))
        )
      )
}

class DiscountRepository(dbConfig: DatabaseConfig = ProductionDatabaseConfig)(implicit ec: ExecutionContext) {
  val discounts = TableQuery[DiscountTable]
  val db = dbConfig.db

  def createTable(): Future[Unit] = {
    db.run(discounts.schema.createIfNotExists)
  }

  def insertDiscount(discount: Discount): Future[Int] = {
    db.run(discounts += discount)
  }

  def fetchDiscounts(): Future[Seq[Discount]] = {
    db.run(discounts.result)
  }

  def dropAll(): Future[Int] = {
    db.run(discounts.delete)
  }

  def dropTable(): Future[Unit] = {
    db.run(discounts.schema.drop)
  }
}

object DiscountRepository {
  def apply()(implicit ec: ExecutionContext): DiscountRepository = new DiscountRepository()
}

