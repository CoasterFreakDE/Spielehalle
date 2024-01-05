package one.devsky.spielehalle.extensions

import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

/**
 * Sets the reply action to be private.
 *
 * This method sets the message action to be private or ephemeral. When a message is sent as a private action,
 * it is only visible to the user who triggered the action and not visible to other users in the conversation.
 *
 * @return The updated [ReplyCallbackAction] with the private flag set.
 */
fun ReplyCallbackAction.private() = this.setEphemeral(true)

/**
 * Schedules the execution of a rest action after a specified duration.
 *
 * @param duration the duration after which the rest action should be executed
 * @param <T> the type of the RestAction result
 */
fun <T> RestAction<T>?.queueAfter(duration: Duration) {
    this?.queueAfter(duration.inWholeMilliseconds, TimeUnit.MILLISECONDS)
}

/**
 * Converts the current string into a code block.
 *
 * @param language The optional language for the code block. Defaults to an empty string.
 * @return The current string surrounded by code block backticks.
 */
fun String?.asCodeBlock(language: String = "") = "```$language\n$this\n```"

fun Any?.asCodeBlock(language: String = "") = "```$language\n$this\n```"