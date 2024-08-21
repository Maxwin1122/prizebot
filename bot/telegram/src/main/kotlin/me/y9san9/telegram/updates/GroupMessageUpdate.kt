package me.y9san9.telegram.updates

import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.delete
import dev.inmo.tgbotapi.types.chat.CommonUser
import dev.inmo.tgbotapi.types.message.abstracts.ContentMessage
import dev.inmo.tgbotapi.types.message.abstracts.FromUserMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.userLink
import me.y9san9.telegram.updates.hierarchies.PossiblyFromUserLocalizedDIBotUpdate
import me.y9san9.telegram.updates.primitives.FromChatUpdate
import me.y9san9.telegram.updates.primitives.HasTextUpdate


class GroupMessageUpdate<out DI> (
    override val bot: TelegramBot,
    override val di: DI,
    private val message: ContentMessage<*>,
) : HasTextUpdate, FromChatUpdate, PossiblyFromUserLocalizedDIBotUpdate<DI> {
    suspend fun delete() = message.delete(bot)

    override val userId = (message as? FromUserMessage)?.user?.id?.chatId?.long
    override val text: String? = (message.content as? TextContent)?.text
    override val chatId = message.chat.id.chatId.long
    override val languageCode = ((message as? FromUserMessage)?.user as? CommonUser)?.languageCode
}
