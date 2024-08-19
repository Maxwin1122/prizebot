package me.y9san9.prizebot.handlers.callback_queries.command

import dev.inmo.tgbotapi.extensions.api.edit.text.editMessageText
import me.y9san9.prizebot.actors.telegram.extractor.GiveawayFromCommandExtractor
import me.y9san9.prizebot.extensions.telegram.getLocale
import me.y9san9.prizebot.extensions.telegram.PrizebotCallbackQueryUpdate
import me.y9san9.telegram.extensions.asTextContentMessage


object DeleteGiveawayCommand {
    suspend fun handle(update: PrizebotCallbackQueryUpdate) {
        val giveaway = GiveawayFromCommandExtractor.extract(update, splitter = "_") ?: return

        val message = update.message?.asTextContentMessage() ?: return
        val userId = update.userId

        if(giveaway.ownerId != userId)
            return

        giveaway.delete()

        update.bot.editMessageText (
            message, entities = update.getLocale().giveawayDeleted(giveaway.title)
        )
        
        update.answer()
    }
}
