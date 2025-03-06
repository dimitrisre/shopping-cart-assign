import db.DatabaseConfig
import slick.jdbc.SQLiteProfile.api.Database

trait DbConfig {
  object TestDatabaseConfig extends DatabaseConfig {
    lazy val db: Database = Database.forURL("jdbc:sqlite::memory:?cache=shared", driver = "org.sqlite.JDBC")
  }
}
