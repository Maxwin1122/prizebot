package me.y9san9.prizebot.database.linked_channels_storage

import org.jetbrains.exposed.sql.Database


fun LinkedChannelsStorage(database: Database): LinkedChannelsStorage = TableLinkedChannelsStorage(database)

interface LinkedChannelsStorage {
    suspend fun linkChannel(userId: Long, channelId: Long)
    suspend fun unlinkChannel(userId: Long, channelId: Long)
    suspend fun getChannels(userId: Long): List<Long>
}
