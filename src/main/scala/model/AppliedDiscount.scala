package model

import db.entities.{Discount, Item}

trait AppliedDiscount {
  val discount: Discount
  def applyLogic(items: List[Item], currentTotalPrice: BigDecimal): BigDecimal

  def apply(items: List[Item], currentTotalPrice: BigDecimal): BigDecimal =
    if(checkConditions(items)) applyLogic(items, currentTotalPrice)
    else currentTotalPrice

  def checkTimeSpan(): Boolean = {
    val currentTimeMillis = System.currentTimeMillis()
    discount.startTime.forall(t => currentTimeMillis >= t) &&
      discount.endTime.forall(t => currentTimeMillis <= t)
  }

  def checkItems(items: List[Item]): Boolean = {
    val groupedDepends = discount.dependsOnItems.groupBy(identity).mapValues(_.length)
    val itemsGrouped = items.groupBy(_.id.getOrElse(0)).mapValues(_.length)

    groupedDepends.forall(dep => itemsGrouped.exists(a => a._1 == dep._1 && a._2 == dep._2))
  }

  def checkConditions(items: List[Item]): Boolean = {
    val cTimeSpan = checkTimeSpan()
    val cItems = checkItems(items)

//    println(s"check time span: $cTimeSpan")
//    println(s"check items: $cItems")
    cTimeSpan && cItems
  }

}

object AppliedDiscount {
  def getAppliedDiscounts(discounts: Seq[Discount]): List[AppliedDiscount] =
    discounts.map { discount =>
      if (discount.percentage.isDefined) new PercentageDiscount(discount) else new AmountDiscount(discount)
    }.toList
}

class PercentageDiscount(val discount: Discount) extends AppliedDiscount {
  override def applyLogic(items: List[Item], currentTotalPrice: BigDecimal): BigDecimal = {
    val discountPercentage = discount.percentage.getOrElse(throw new RuntimeException(s"Discount ${discount.id}, ${discount.name} has undefined percentage"))

    discount.forItem.map{ itemId =>
      val affectedItems = items.filter(_.id.exists(_ == itemId))
      val currentItemsPrice = affectedItems.foldLeft(BigDecimal(0.0))((sum, i) => sum + i.price)
      val newItemsPrice = currentItemsPrice - currentItemsPrice*discountPercentage

      currentTotalPrice - currentItemsPrice + newItemsPrice
    }.getOrElse(currentTotalPrice - currentTotalPrice*discountPercentage)
  }
}

class AmountDiscount(val discount: Discount) extends AppliedDiscount {

  override def applyLogic(items: List[Item], currentTotalPrice: BigDecimal): BigDecimal = {
    val discountAmount = discount.amount.getOrElse(throw new RuntimeException(s"Discount ${discount.id}, ${discount.name} has undefined amount"))

    discount.forItem.map{ itemId =>
      val affectedItems = items.filter(_.id.exists(_ == itemId))
      val currentItemsPrice = affectedItems.foldLeft(BigDecimal(0.0))((sum, i) => sum + i.price)
      val newItemsPrice = currentItemsPrice - affectedItems.size*discountAmount

      currentTotalPrice - currentItemsPrice + newItemsPrice
    }.getOrElse(currentTotalPrice - discountAmount)
  }
}
