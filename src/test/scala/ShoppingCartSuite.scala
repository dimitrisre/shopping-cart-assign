import db.entities.{Discount, Item}
import db.tables.{DiscountRepository, ItemRepository}
import model.{ItemDTO, ShoppingCart}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

class ShoppingCartSuite extends AsyncFlatSpec with Matchers with ScalaFutures with BeforeAndAfterAll with DbConfig {
  lazy val discountRepository: DiscountRepository = new DiscountRepository(TestDatabaseConfig)
  lazy val itemRepository: ItemRepository = new ItemRepository(TestDatabaseConfig)
  implicit val ec: ExecutionContext = ExecutionContext.global

  override def beforeAll(): Unit = {
    Await.result(
      for{
        _ <- discountRepository.createTable()
        _ <- itemRepository.createTable()
        _ <- itemRepository.insert(Item(None, "Apples", "bag", 1.0))
        _ <- itemRepository.insert(Item(None, "Soup", "tin", 0.65))
        _ <- itemRepository.insert(Item(None, "Bread", "loaf", 0.8))
        _ <- itemRepository.insert(Item(None, "Milk", "bottle", 1.3))
      } yield (), 5.seconds
    )
    super.beforeAll()
  }

  "Shopping cart" should "Calculate correct the total price with no discount" in {
    val items = Await.result(
      Future.sequence(
      List(
        ItemDTO("Milk"),
        ItemDTO("Apples"),
        ItemDTO("Bread"),
        ItemDTO("Apples")
      ).map(i => ItemDTO.getItem(i, itemRepository))
    ).map(_.flatten), 1.second)

    val discounts = Await.result(discountRepository.fetchDiscounts(), 1.second)

    val shoppingCart = new ShoppingCart(items, discounts.toList)
    val (beforePrice, afterPrice, descriptions) = shoppingCart.calculateTotal()

    beforePrice shouldBe 1.3+2.0+0.8
    afterPrice shouldBe beforePrice
  }

  "Shopping cart" should "Calculate correct the total price with global discount" in {
    val items = Await.result(
      Future.sequence(
        List(
          ItemDTO("Milk"),
          ItemDTO("Apples"),
          ItemDTO("Bread"),
          ItemDTO("Apples")
        ).map(i => ItemDTO.getItem(i, itemRepository))
      ).map(_.flatten), 1.second)

    Await.result(discountRepository.insertDiscount(
      Discount(
        None,
        "Global discount 10%", "Global discount 10%",
        None, None, None, Some(BigDecimal(0.10)), None, Array.empty
      )), 1.second)

    val discounts = Await.result(discountRepository.fetchDiscounts(), 1.second)
    val shoppingCart = new ShoppingCart(items, discounts.toList)
    val (beforePrice, afterPrice, descriptions) = shoppingCart.calculateTotal()

    beforePrice shouldBe 1.3+2.0+0.8
    afterPrice shouldBe (beforePrice - beforePrice * BigDecimal(0.10))
  }

  "Shopping cart" should "Calculate correct the total price with discount on milk if 2 bags of apples are purchased" in {
    Await.result(discountRepository.dropAll(), 1.second)

    val items = Await.result(
      Future.sequence(
        List(
          ItemDTO("Milk"),
          ItemDTO("Apples"),
          ItemDTO("Bread"),
          ItemDTO("Apples")
        ).map(i => ItemDTO.getItem(i, itemRepository))
      ).map(_.flatten), 1.second)

    Await.result(discountRepository.insertDiscount(
      Discount(
        None,
        "2 bags of Apples, Milk 10%",
        "2 bags of Apples, Milk 10%",
        None,
        None,
        None,
        Some(BigDecimal(0.10)),
        Some(4),
        Array(1, 1)
      )), 1.second)

    val discounts = Await.result(discountRepository.fetchDiscounts(), 1.second)
    val shoppingCart = new ShoppingCart(items, discounts.toList)
    val (beforePrice, afterPrice, descriptions) = shoppingCart.calculateTotal()

    beforePrice shouldBe 1.3+2.0+0.8
    afterPrice shouldBe (beforePrice - BigDecimal(1.3*0.10))
  }

  "Shopping cart" should "Calculate correct the total price with discount on milk if 2 bags of apples are purchased and Global amount 1 euro" in {
    Await.result(discountRepository.dropAll(), 1.second)

    val items = Await.result(
      Future.sequence(
        List(
          ItemDTO("Milk"),
          ItemDTO("Apples"),
          ItemDTO("Bread"),
          ItemDTO("Apples")
        ).map(i => ItemDTO.getItem(i, itemRepository))
      ).map(_.flatten), 1.second)

    Await.result(discountRepository.insertDiscount(
      Discount(
        None,
        "2 bags of Apples, Milk 10%",
        "2 bags of Apples, Milk 10%",
        None,
        None,
        None,
        Some(BigDecimal(0.10)),
        Some(4),
        Array(1, 1)
      )), 1.second)
    Await.result(discountRepository.insertDiscount(
      Discount(
        None,
        "Global discount 1 euro", "Global discount 1 euro",
        None, None, Some(BigDecimal(1.0)), None, None, Array.empty
      )), 1.second)

    val discounts = Await.result(discountRepository.fetchDiscounts(), 1.second)
    val shoppingCart = new ShoppingCart(items, discounts.toList)
    val (beforePrice, afterPrice, descriptions) = shoppingCart.calculateTotal()

    beforePrice shouldBe 1.3+2.0+0.8
    afterPrice shouldBe (beforePrice - BigDecimal(1.3*0.10)-1)
  }

  override def afterAll(): Unit = {
    discountRepository.dropTable()
    itemRepository.dropTable()
  }
}
