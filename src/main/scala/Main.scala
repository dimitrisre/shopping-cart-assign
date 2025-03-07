import db.entities.{Discount, Item}
import db.tables.{DiscountRepository, ItemRepository}
import model.{AppliedDiscount, ItemDTO, ShoppingCart}

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
object Main {
  def main(args: Array[String]): Unit = {
    val itemsDTO = args.map(a => ItemDTO(a)).toList

    val itemsRepository = new ItemRepository()
    val discountRepository = new DiscountRepository()
//    val currentTime = Instant.now().toEpochMilli
//    val oneWeekEnd = Instant.now().plus(7, ChronoUnit.DAYS).toEpochMilli
//
//    Await.result(
//      for {
//        _ <- discountRepository.createTable()
//        _ <- discountRepository.insertDiscount(
//          Discount(
//            None,
//            "Bread half price",
//            "Buy 2 tins of soup and get a loaf of bread for half price",
//            None,
//            None,
//            None,
//            Some(BigDecimal(0.50)),
//            Some(3),
//            Array(2, 2)
//          )
//        )
//        _ <- discountRepository.insertDiscount(
//          Discount(
//            None,
//            "Apples 10% off",
//            "Apples have a 10% discount off their normal price this week",
//            Some(currentTime),
//            Some(oneWeekEnd),
//            None,
//            Some(BigDecimal(0.10)),
//            Some(1),
//            Array.empty
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

