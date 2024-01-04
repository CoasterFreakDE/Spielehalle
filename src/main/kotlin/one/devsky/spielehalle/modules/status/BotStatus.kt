package one.devsky.spielehalle.modules.status

/**
 * Represents the status with emojis.
 *
 * @param status The status string.
 * @param emojis The list of emojis associated with the status. Defaults to an empty list.
 */
data class BotStatus(
    val status: String,
    val emojis: List<String> = emptyList()
)
