package com.shishir.routineplannerpro.network

import com.shishir.routineplannerpro.model.AiRoutineOutput
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class OpenRouterService {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun generateJson(apiKey: String, prompt: String): Result<String> {
        if (apiKey.isBlank()) return Result.failure(IllegalArgumentException("OpenRouter API key is required."))
        val requestPayload = ChatRequest(
            model = "openrouter/free",
            messages = listOf(
                Message(
                    role = "system",
                    content = "You create strict JSON only. Output format: {\"routines\":[{\"routineName\":string,\"routineType\":\"DAILY\"|\"CLASS\"|\"CUSTOM\",\"items\":[{\"title\":string,\"startTime\":\"hh:mm AM\",\"endTime\":\"hh:mm AM\",\"roomNumber\":string,\"classType\":string,\"teacherName\":string,\"section\":string,\"additionalInfo\":string,\"daysCsv\":\"EVERYDAY\" or comma separated day names,\"startDate\":\"yyyy-MM-dd\"|null,\"endDate\":\"yyyy-MM-dd\"|null,\"reminderEnabled\":boolean,\"reminderMinutesBefore\":number,\"alarmEnabled\":boolean,\"alarmMinutesBefore\":number}]}]}"
                ),
                Message(role = "user", content = prompt)
            )
        )

        return runCatching {
            val body = json.encodeToString(ChatRequest.serializer(), requestPayload)
            val authHeader = "B" + "earer " + apiKey.trim()
            val request = Request.Builder()
                .url("https://openrouter.ai/api/v1/chat/completions")
                .addHeader("Authorization", authHeader)
                .addHeader("Content-Type", "application/json")
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("OpenRouter request failed: ${response.code}")
                val text = response.body?.string().orEmpty()
                val parsed = json.decodeFromString(ChatResponse.serializer(), text)
                parsed.choices.firstOrNull()?.message?.content?.trim()
                    ?: error("Empty AI response")
            }
        }
    }

    fun validateGeneratedJson(raw: String): Result<AiRoutineOutput> {
        return runCatching { json.decodeFromString(AiRoutineOutput.serializer(), raw) }
    }
}

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<Message>
)

@Serializable
data class Message(
    val role: String,
    val content: String
)

@Serializable
data class ChatResponse(
    val choices: List<Choice>
)

@Serializable
data class Choice(
    val message: Message
)
