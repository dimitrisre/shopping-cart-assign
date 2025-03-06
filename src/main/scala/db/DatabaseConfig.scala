package db
import slick.jdbc.SQLiteProfile.api._

trait DatabaseConfig {
  val db: Database
}

object ProductionDatabaseConfig extends DatabaseConfig {
  val db: Database = Database.forURL("jdbc:sqlite:shopping_cart.db", driver = "org.sqlite.JDBC")
}