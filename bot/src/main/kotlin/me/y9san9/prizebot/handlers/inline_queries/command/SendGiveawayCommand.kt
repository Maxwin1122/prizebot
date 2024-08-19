package me.y9san9.prizebot.handlers.inline_queries.command

import me.y9san9.prizebot.actors.telegram.extractor.GiveawayFromCommandExtractor
import me.y9san9.prizebot.actors.telegram.mapper.GiveawayToResultArticleMapper
import me.y9san9.prizebot.extensions.telegram.getLocale
import me.y9san9.prizebot.extensions.telegram.PrizebotInlineQueryUpdate


object SendGiveawayCommand {
    suspend fun handle(update: PrizebotInlineQueryUpdate) {
        val giveaway = GiveawayFromCommandExtractor.extract(update, splitter = "_")
            ?.takeIf { it.ownerId == update.userId } ?: return handleNullGiveaway(update)

        update.answer (
            GiveawayToResultArticleMapper.map(resultId = "${giveaway.id}", update, giveaway),
            cachedTime = 0,
            isPersonal = true
        )
    }

    private suspend fun handleNullGiveaway(update: PrizebotInlineQueryUpdate) = update.answer (
        switchPmText = update.getLocale().giveawayDoesNotExist,
        switchPmParameter = "giveaway_does_not_exist",
        cachedTime = 0,
        isPersonal = true
    )
}
