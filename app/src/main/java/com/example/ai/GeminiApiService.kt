package com.example.ai

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class ThinkingConfig(
    @Json(name = "thinkingLevel") val thinkingLevel: String = "HIGH"
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "thinkingConfig") val thinkingConfig: ThinkingConfig = ThinkingConfig(thinkingLevel = "HIGH")
)

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig = GenerationConfig()
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content?
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<Candidate>?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.1-pro-preview:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun analyzeGameOrStrategy(prompt: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val isPersian = java.util.Locale.getDefault().language.startsWith("fa", ignoreCase = true)
        
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return if (isPersian) {
                "کلید Gemini API هنوز تنظیم نشده است. لطفاً کلید API را در تنظیمات وارد کنید."
            } else {
                "Gemini API key is not configured. Please set your API key in settings."
            }
        }

        val systemInstruction = if (isPersian) {
            """
            شما یک استاد و تحلیل‌گر حرفه‌ای و هوشمند بازی کارت "شلم" (Shelem) هستید.
            پاسخ‌ها را کاملاً به زبان فارسی روان، دقیق، محترمانه و کاربردی ارائه دهید.
            اگر از شما تحلیل دست جاری خواسته شده، وضعیت امتیازات، ریسک خواندن، پیشنهاد حاکم شدن و استراتژی کارت‌بازی را بررسی کنید.
            """.trimIndent()
        } else {
            """
            You are a master strategist and expert analyst for the trick-taking card game "Shelem".
            Provide clear, practical, structured, and insightful game strategy and analysis in English.
            If asked to analyze a match state, evaluate point margins, bidding risks, trump selection, and tactical advice.
            """.trimIndent()
        }

        val request = GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(
                            text = """
                                $systemInstruction
                                
                                User Query/Match Data:
                                $prompt
                            """.trimIndent()
                        )
                    )
                )
            ),
            generationConfig = GenerationConfig(
                thinkingConfig = ThinkingConfig(thinkingLevel = "HIGH")
            )
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: (if (isPersian) "پاسخی از هوش مصنوعی دریافت نشد." else "No response received from AI.")
        } catch (e: Exception) {
            if (isPersian) {
                "خطا در ارتباط با هوش مصنوعی: ${e.localizedMessage ?: "مشکل در شبکه"}"
            } else {
                "Error communicating with AI: ${e.localizedMessage ?: "Network error"}"
            }
        }
    }
}
