package db.entities
import io.circe.syntax._
import io.circe.parser._

/**
 * Discount entity
 * @param id: Option[Int] - discount id
 * @param name: String - discount name
 * @param description: String - discount description
 * @param startTime: Option[Long] - starting time the discount is valid from
 * @param endTime: Option[Long] - end date the discount is valid until
 * @param amount: Option[BigDecimal] - discount constant amount
 * @param percentage: Option[BigDecimal] - discount percentage amount
 * @param forItem: Option[Int] - On which item (itemId) of the cart the discount is applied. If empty this is a global discount
 * @param dependsOnItems: Array[Int] - The discount is valid if those items are in the cart
 */
case class Discount(id: Option[Int],
                    name: String,
                    description: String,
                    startTime: Option[Long],
                    endTime: Option[Long],
                    amount: Option[BigDecimal],
                    percentage: Option[BigDecimal],
                    forItem: Option[Int],
                    dependsOnItems: Array[Int]
                   )

object Discount {
  def dependsOnItemsDecode(dependsOnItemsJson: String): Array[Int] ={
    decode[Array[Int]](dependsOnItemsJson).getOrElse(Array.empty[Int])
  }

  def dependsOnItemsEncode(dependsOnItemsArray: Array[Int]): String = {
    dependsOnItemsArray.asJson.noSpaces
  }
}
