package me.y9san9.prizebot.handlers.choosen_inline_result.commands

import me.y9san9.prizebot.extensions.telegram.PrizebotChosenInlineResultUpdate
import me.y9san9.telegram.updates.extensions.command.command


object SendGiveawayCommand {
    fun handle(update: PrizebotChosenInlineResultUpdate) {
        val giveawayId = update.command(splitter = "_")
            ?.args?.getOrNull(index = 0)
            ?.toLongOrNull() ?: return
        
        update.di.addActiveMessage (
            giveawayId = giveawayId,
            inlineMessageId = update.inlineMessage ?: return
        )
    }
}
