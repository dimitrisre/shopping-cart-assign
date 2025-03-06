import db.entities.{Discount, Item}
import db.tables.{DiscountRepository, ItemRepository}
import model.{AppliedDiscount, ItemDTO, ShoppingCart}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
object Main {
  def main(args: Array[String]): Unit = {
    val itemsDTO = args.map(a => ItemDTO(a)).toList

    val itemsRepository = new ItemRepository()
    val discountRepository = new DiscountRepository()

//    Await.result(
//      for {
//        _ <- discountRepository.createTable()
//        _ <- discountRepository.insertDiscount(
//          Discount(
//            None,
//            "2 bags of Apples, Milk 10%",
//            "2 bags of Apples, Milk 10%",
//            None,
//            None,
//            None,
//            Some(BigDecimal(0.10)),
//            Some(4),
//            Array(1, 1)
//          )
//        )
//      } yield ()
//    ,10.seconds)

//    for{
//      _ <- itemsRepository.createTable()
//      _ <- itemsRepository.insert(Item(None, "Apples", "bag", 1.0))
//      _ <- itemsRepository.insert(Item(None, "Soup", "tin", 0.65))
//      _ <- itemsRepository.insert(Item(None, "Bread", "loaf", 0.8))
//      _ <- itemsRepository.insert(Item(None, "Milk", "bottle", 1.3))
//    } yield ()

    itemsRepository.fetchItems().foreach(items =>
      println(
        s"""Items inventory
           |${items.map(i => s"${i.name} - ${i.price} Euros per ${i.unit}").mkString("\n")}
           |""".stripMargin)
    )

    val discountsF = discountRepository.fetchDiscounts()
    discountsF.foreach(discounts =>
      println(
        s"""Discounts available
           |${discounts.map(d => s"${d.description}").mkString("\n")}
           |""".stripMargin)
    )

    val itemsF =
      Future.sequence(
        itemsDTO.map(dto =>
          ItemDTO
            .getItem(dto, itemsRepository)
            .map(_.getOrElse(throw new RuntimeException(s"Item with name: ${dto.name} does not exist in our database")))
        ))

    itemsF.foreach(items =>
      println(
        s"""You selected
           |${items.groupBy(_.id).map(i => s"${i._2.head.name}: ${i._2.length} ${i._2.head.unit}").mkString("\n")}
           |""".stripMargin)

    )

    val shoppingCart = for{
      items <- itemsF
      discounts <- discountsF
    } yield new ShoppingCart(items, discounts.toList)

    Await.result(shoppingCart.map(_.calculateAndPrint()), 60.seconds)
  }
}

