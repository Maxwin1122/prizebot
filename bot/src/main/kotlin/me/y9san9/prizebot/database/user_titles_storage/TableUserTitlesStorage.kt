package me.y9san9.prizebot.database.user_titles_storage

import me.y9san9.extensions.any.unit
import me.y9san9.prizebot.database.user_titles_storage.TableUserTitlesStorage.UserTitles.USER_ID
import me.y9san9.prizebot.database.user_titles_storage.TableUserTitlesStorage.UserTitles.USER_TITLE
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction


internal class TableUserTitlesStorage (
    private val database: Database
) : UserTitlesStorage {
    private object UserTitles : Table(name = "user_titles") {
        val USER_ID = long("userId")
        val USER_TITLE = text("userTitle")
    }

    init {
        transaction(database) {
            SchemaUtils.create(UserTitles)
        }
    }

    override fun saveUserTitle(userId: Long, title: String) = transaction(database) {
        UserTitles.deleteWhere { USER_ID eq userId }
        UserTitles.insert {
            it[USER_ID] = userId
            it[USER_TITLE] = title
        }
    }.unit

    override fun getUserTitle(userId: Long) = transaction(database) {
        UserTitles.selectAll().where { USER_ID eq userId }.firstOrNull()?.get(USER_TITLE)
    }
}