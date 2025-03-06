package model

import db.entities.{Discount, Item}

class ShoppingCart(items: List[Item], discounts: List[Discount]){
  def calculateTotal(): (BigDecimal, BigDecimal, Seq[String]) = {
    val appliedDiscounts = AppliedDiscount.getAppliedDiscounts(discounts)

    val currentTotal = items.foldLeft(BigDecimal(0.0))((sum, i) => sum + i.price)

    val (discountedTotal, discountsDescriptions) = appliedDiscounts.foldLeft((currentTotal, List("(No offers available)"))){(stepAcc, discount) =>
      val (affectedPrice, stepDescriptions) = stepAcc

      val afterPrice = discount(items, affectedPrice)
      val difference = affectedPrice - afterPrice

      val descriptions = if(difference > 0)
        stepDescriptions :+ s"${discount.discount.description} off: $difference"
      else stepDescriptions

      (afterPrice, descriptions)
    }

    val descriptions = if(discountsDescriptions.length > 1)
      discountsDescriptions.tail
    else discountsDescriptions

    (currentTotal, discountedTotal, descriptions)
  }

  def calculateAndPrint(): Unit = {
    val (beforePrice, afterPrice, descriptions) = calculateTotal()

    println(
      s"""Subtotal:${beforePrice} euros
         |${descriptions.mkString("\n")}
         |Total Price: ${afterPrice} euros
         |""".stripMargin)
  }
}
