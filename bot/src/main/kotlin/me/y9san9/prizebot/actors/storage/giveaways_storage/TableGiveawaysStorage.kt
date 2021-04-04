package me.y9san9.prizebot.actors.storage.giveaways_storage

import me.y9san9.prizebot.actors.storage.giveaways_storage.TableGiveawaysStorage.Giveaways.GIVEAWAY_ID
import me.y9san9.prizebot.actors.storage.giveaways_storage.TableGiveawaysStorage.Giveaways.GIVEAWAY_LANGUAGE_CODE
import me.y9san9.prizebot.actors.storage.giveaways_storage.TableGiveawaysStorage.Giveaways.GIVEAWAY_OWNER_ID
import me.y9san9.prizebot.actors.storage.giveaways_storage.TableGiveawaysStorage.Giveaways.GIVEAWAY_PARTICIPATE_BUTTON
import me.y9san9.prizebot.actors.storage.giveaways_storage.TableGiveawaysStorage.Giveaways.GIVEAWAY_RAFFLE_DATE
import me.y9san9.prizebot.actors.storage.giveaways_storage.TableGiveawaysStorage.Giveaways.GIVEAWAY_TITLE
import me.y9san9.prizebot.actors.storage.giveaways_storage.TableGiveawaysStorage.Giveaways.GIVEAWAY_WINNER_ID
import me.y9san9.prizebot.extensions.any.unit
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.OffsetDateTime


internal class TableGiveawaysStorage (
    private val database: Database
) : GiveawaysStorage {
    private object Giveaways : Table(name = "giveaways") {
        val GIVEAWAY_ID = long("id").autoIncrement()
        val GIVEAWAY_OWNER_ID = long("ownerId")
        val GIVEAWAY_TITLE = text("title")
        val GIVEAWAY_PARTICIPATE_BUTTON = text("participateButton")
        val GIVEAWAY_RAFFLE_DATE = text("raffleDate").nullable()
        val GIVEAWAY_LANGUAGE_CODE = text("languageCode").nullable()
        val GIVEAWAY_WINNER_ID = long("winnerId").nullable()
    }

    init {
        transaction(database) {
            SchemaUtils.create(Giveaways)
        }
    }

    override fun getGiveawayById(id: Long): Giveaway? = transaction(database) {
        Giveaways.select { GIVEAWAY_ID eq id }.firstOrNull()?.toGiveaway()
    }

    override fun saveGiveaway (
        ownerId: Long,
        title: String,
        participateButton: String,
        languageCode: String?,
        raffleDate: OffsetDateTime?
    ) = transaction(database) {
        Giveaways.insert {
            it[GIVEAWAY_OWNER_ID] = ownerId
            it[GIVEAWAY_TITLE] = title
            it[GIVEAWAY_PARTICIPATE_BUTTON] = participateButton
            it[GIVEAWAY_LANGUAGE_CODE] = languageCode
            it[GIVEAWAY_RAFFLE_DATE] = raffleDate?.toString()
        }
    }.let { data ->
        ActiveGiveaway (
            id = data[GIVEAWAY_ID],
            ownerId, title, participateButton,
            languageCode, raffleDate
        )
    }

    override fun removeRaffleDate(giveawayId: Long) = transaction(database) {
        Giveaways.update({ GIVEAWAY_ID eq giveawayId }) {
            it[GIVEAWAY_RAFFLE_DATE] = null
        }
    }.unit

    override fun finishGiveaway(giveawayId: Long, winnerId: Long) = transaction(database) {
        Giveaways.update({ GIVEAWAY_ID eq giveawayId }) {
            it[GIVEAWAY_WINNER_ID] = winnerId
        }
    }.unit

    override fun getUserGiveaways(ownerId: Long, count: Int, offset: Long) = transaction(database) {
        Giveaways.select { GIVEAWAY_OWNER_ID eq ownerId }
            .orderBy(GIVEAWAY_ID, order = SortOrder.DESC)
            .limit(n = count, offset = offset)
            .map { it.toGiveaway() }
    }

    override fun getAllGiveaways() = transaction(database) {
        Giveaways.selectAll().map { it.toGiveaway() }
    }

    override fun deleteGiveaway(id: Long) = transaction(database) {
        Giveaways.deleteWhere { GIVEAWAY_ID eq id }
        return@transaction
    }

    private fun ResultRow.toGiveaway() = if(this[GIVEAWAY_WINNER_ID] == null)
        ActiveGiveaway (
            this[GIVEAWAY_ID],
            this[GIVEAWAY_OWNER_ID],
            this[GIVEAWAY_TITLE],
            this[GIVEAWAY_PARTICIPATE_BUTTON],
            this[GIVEAWAY_LANGUAGE_CODE],
            this[GIVEAWAY_RAFFLE_DATE]?.let(OffsetDateTime::parse)
        )
    else
        FinishedGiveaway (
            this[GIVEAWAY_ID],
            this[GIVEAWAY_OWNER_ID],
            this[GIVEAWAY_TITLE],
            this[GIVEAWAY_PARTICIPATE_BUTTON],
            this[GIVEAWAY_LANGUAGE_CODE],
            this[GIVEAWAY_RAFFLE_DATE]?.let(OffsetDateTime::parse),
            winnerId = this[GIVEAWAY_WINNER_ID] ?: error("Finished giveaway must contain winnerId")
        )
}
