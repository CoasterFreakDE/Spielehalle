package one.devsky.spielehalle.openai

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.image.ImageCreation
import com.aallam.openai.api.image.ImageSize
import com.aallam.openai.api.image.ImageURL
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import one.devsky.spielehalle.utils.Environment
import kotlin.time.Duration.Companion.seconds

object OpenAIHandler {

    private val openAI = OpenAI(
        Environment.getEnv("OPENAI_API_KEY") ?: error("No OpenAI key provided"),
        timeout = Timeout(60.seconds)
    )


    suspend fun getSingleAnswer(question: String, context: String, maxTokens: Int? = null): String {
        val response = openAI.chatCompletion(
            ChatCompletionRequest(
                model = ModelId("gpt-4-1106-preview"),
                messages = listOf(
                    ChatMessage(ChatRole.User, question),
                    ChatMessage(ChatRole.Assistant, context)
                ),
                maxTokens = maxTokens,
            )
        )
        return response.choices.first().message.content ?: error("No response from OpenAI")
    }

    suspend fun getImage(prompt: String): List<ImageURL> {
        return openAI.imageURL(
            creation = ImageCreation(
                prompt = prompt,
                model = ModelId("dall-e-3"),
                n = 1,
                size = ImageSize.is1024x1024
            )
        )
    }
}