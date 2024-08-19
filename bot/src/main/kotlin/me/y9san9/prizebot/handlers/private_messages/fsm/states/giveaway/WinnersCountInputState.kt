package me.y9san9.prizebot.handlers.private_messages.fsm.states.giveaway

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.y9san9.fsm.FSMStateResult
import me.y9san9.fsm.stateResult
import me.y9san9.prizebot.database.giveaways_storage.CheckedWinnersCount
import me.y9san9.prizebot.database.giveaways_storage.WinnersCount
import me.y9san9.extensions.any.unit
import me.y9san9.extensions.offset_date_time.OffsetDateTimeSerializer
import me.y9san9.prizebot.database.giveaways_storage.WinnersSettings
import me.y9san9.prizebot.extensions.telegram.PrizebotFSMState
import me.y9san9.prizebot.extensions.telegram.PrizebotPrivateMessageUpdate
import me.y9san9.prizebot.extensions.telegram.getLocale
import me.y9san9.prizebot.extensions.telegram.textOrDefault
import me.y9san9.prizebot.handlers.private_messages.fsm.states.MainState
import me.y9san9.telegram.updates.extensions.send_message.sendMessage
import java.time.OffsetDateTime


@SerialName("winners_count_input")
@Serializable
data class WinnersCountInputData (
    val title: String,
    val participateText: String,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val raffleDate: OffsetDateTime?
)

object WinnersCountInputState : PrizebotFSMState<WinnersCountInputData> {
    override suspend fun process (
        data: WinnersCountInputData,
        event: PrizebotPrivateMessageUpdate
    ): FSMStateResult<*> {

        suspend fun next(winnersCount: WinnersCount = WinnersCount.create(1)) =
            if(winnersCount.value in 2..10)
                DisplayWinnersWithEmojisInputData(
                    event,
                    DisplayWinnersWithEmojisInputData (
                        data.title,
                        data.participateText,
                        data.raffleDate,
                        winnersCount
                    )
                )
            else ConditionInputState (
                event,
                ConditionInputData (
                    data.title, data.participateText, data.raffleDate,
                    WinnersSettings.create(winnersCount, displayWithEmojis = false)
                )
            )

        event.textOrDefault { text ->
            when (text) {
                "/cancel" -> return MainState.cancellation(event)
                "/skip" -> return next()
            }

            val number = text.toIntOrNull() ?: return@textOrDefault event.sendMessage(event.getLocale().enterNumber).unit

            when (val winnersCount = WinnersCount.createChecked(number)) {
                is CheckedWinnersCount.OutOfRange ->
                    return@textOrDefault event.sendMessage(event.getLocale().winnersCountIsOutOfRange).unit
                is WinnersCount -> return next(winnersCount)
            }
        }

        return stateResult(WinnersCountInputState, data)
    }
}

@Suppress("FunctionName")
suspend fun WinnersCountInputState (
    update: PrizebotPrivateMessageUpdate,
    data: WinnersCountInputData
) = stateResult(WinnersCountInputState, data) {
    update.sendMessage (
        update.getLocale().enterWinnersCount
    )
}
