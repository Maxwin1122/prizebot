package me.y9san9.prizebot.handlers.callback_queries.command

import me.y9san9.prizebot.actors.telegram.updater.SelectLocaleUpdater
import me.y9san9.prizebot.extensions.telegram.PrizebotCallbackQueryUpdate
import me.y9san9.prizebot.extensions.telegram.getLocale
import me.y9san9.telegram.updates.extensions.command.command


object SelectLocaleCommand {
    suspend fun handle (
        update: PrizebotCallbackQueryUpdate,
        splitter: String = "_"
    ) {
        val command = update.command(splitter) ?: return

        update.di.setLanguageCode(update.userId, command.args[0])

        SelectLocaleUpdater.update(update)

        update.answer(update.getLocale().localeSelected)
    }
}
