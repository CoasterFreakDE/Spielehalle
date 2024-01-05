package one.devsky.spielehalle.utils

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import one.devsky.spielehalle.Spielehalle
import one.devsky.spielehalle.db.model.enums.LogType

/**
 * Throws an exception message to a Discord channel.
 *
 * @param exception The exception to be thrown to the Discord channel.
 */
fun throwErrorToDiscord(exception: Exception) {
    // Throw to discord
    sendToChannel("MAIN_GUILD", "CHANNEL_LOG", "Ein Fehler ist aufgetreten: ${exception.message}")
}


/**
 * Sends a message to a specified channel in a guild.
 *
 * @param guildEnv The environment key for the guild ID.
 * @param channelEnv The environment key for the channel ID.
 * @param message The message to be sent.
 */
fun sendToChannel(guildEnv: String = "MAIN_GUILD", channelEnv: String, message: String): MessageCreateAction? {
    return sendToChannel(guildEnv, channelEnv, MessageCreateData.fromContent(message))
}

/**
 * Sends a message to a specified channel in a guild.
 *
 * @param guildEnv The environment key for the guild ID.
 * @param channelEnv The environment key for the channel ID.
 * @param message The message to be sent.
 */
fun sendToChannel(guildEnv: String = "MAIN_GUILD", channelEnv: String, message: MessageCreateData): MessageCreateAction? {
    Environment.getEnv(guildEnv)?.let { guildId ->
        Environment.getEnv(channelEnv)?.let { channelId ->
            val channel = Spielehalle.instance.jda.getGuildById(guildId)?.getTextChannelById(channelId)
            return channel?.sendMessage(message)
        }
    }
    return null
}

/**
 * Sends a message embed to the specified channel in a guild.
 *
 * @param guildId The environment key for the guild ID.
 * @param channelId The environment key for the channel ID.
 * @param embed The embed to be sent.
 */
fun sendToChannel(guildId: String, channelId: String, embed: MessageEmbed): MessageCreateAction? {
    return sendToChannel(guildId, channelId, MessageCreateData.fromEmbeds(embed))
}

/**
 * Sends a log message as an embedded message to a specified channel in a guild.
 *
 * @param message The log message to be sent as the description of the embedded message.
 * @param logType The type of log, defaulting to LogType.CUSTOM.
 * @return The MessageCreateAction object representing the action of sending the message, or null if the message cannot be sent.
 */
fun sendLogAsEmbed(message: String, logType: LogType = LogType.CUSTOM): MessageCreateAction? {
    return sendLogEmbed(
        EmbedBuilder()
            .setDescription(message)
            .setFooter("Spielehalle | Logs", Spielehalle.instance.jda.selfUser.effectiveAvatarUrl)
        , logType = logType)
}

/**
 * Sends a log message as an embedded message to a specified channel in a guild.
 *
 * @param embed The EmbedBuilder object representing the embedded message to be sent.
 * @return The MessageCreateAction object representing the action of sending the message, or null if the message cannot be sent.
 */
fun sendLogEmbed(vararg embed: EmbedBuilder, otherEmbeds: List<MessageEmbed> = listOf(), logType: LogType = LogType.CUSTOM): MessageCreateAction? {
    if (logType != LogType.CUSTOM) {
        embed.forEach { it.setColor(logType.color) }
    }
    return sendToChannel("MAIN_GUILD", "CHANNEL_LOG", MessageCreateData.fromEmbeds(embed.map { it.build() }))?.addEmbeds(otherEmbeds)
}

/**
 * Sends a direct message to a user with the provided user ID and embed.
 *
 * @param userId The ID of the user to send the direct message to.
 * @param embed The embed to be sent.
 * @return The message create action of the sent message, or null if an error occurs.
 */
fun sendDMToUser(userId: String, embed: EmbedBuilder): MessageCreateAction? {
    try {
        val user = Spielehalle.instance.jda.retrieveUserById(userId).complete() ?: throw Exception("User not found.")
        val privateChannel = user.openPrivateChannel().complete() ?: throw Exception("Unable to open a private channel.")
        return privateChannel.sendMessageEmbeds(embed.build())
    } catch (e: Exception) {
        throwErrorToDiscord(e)
        return null
    }
}