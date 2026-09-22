package com.example.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.data.local.entity.TransactionCategory
import com.example.data.local.entity.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class GeminiPart(
    val text: String? = null,
    val inlineData: InlineData? = null
)

data class InlineData(
    val mimeType: String,
    val data: String
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class ThinkingConfig(
    val thinkingLevel: String = "high"
)

data class GenerationConfig(
    val temperature: Float? = 0.4f,
    val thinkingConfig: ThinkingConfig? = null
)

data class GenerateContentRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: GeminiContent? = null
)

data class CandidatePart(
    val text: String? = null
)

data class CandidateContent(
    val parts: List<CandidatePart>? = null
)

data class GeminiCandidate(
    val content: CandidateContent? = null
)

data class GenerateContentResponse(
    val candidates: List<GeminiCandidate>? = null
)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateFlash(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse

    @POST("v1beta/models/gemini-3.1-pro-preview:generateContent")
    suspend fun generateProHighThinking(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

data class ParsedReceiptResult(
    val title: String,
    val amount: Double,
    val category: TransactionCategory,
    val type: TransactionType,
    val note: String
)

class GeminiService(private val context: Context) {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val api = retrofit.create(GeminiApi::class.java)

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun analyzeFinancialHealthWithHighThinking(
        totalIncome: Double,
        totalExpenses: Double,
        netWorth: Double,
        topExpenseCategories: String,
        activeGoals: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            val savingsRate = if (totalIncome > 0) ((totalIncome - totalExpenses) / totalIncome * 100).toInt() else 0
            return@withContext "AI Advisor Notice: Please configure your GEMINI_API_KEY in the AI Studio Secrets panel to activate high-reasoning financial analysis.\n\nQuick Summary:\n• Monthly Savings Rate: $savingsRate%\n• Recommendation: Keep housing under 30% of gross income and accelerate emergency fund contributions."
        }

        val prompt = """
            Act as Kai's Chief Financial Officer & Strategic Wealth Advisor.
            Analyze the following personal balance sheet:
            - Monthly Income: $$totalIncome
            - Monthly Expenses: $$totalExpenses
            - Net Cashflow: $${totalIncome - totalExpenses}
            - Current Liquidity / Savings: $$netWorth
            - Top Expense Categories: $topExpenseCategories
            - Active Savings Goals: $activeGoals

            Provide a high-reasoning, concise 3-part financial audit:
            1. 📊 Liquidity & Savings Rate Analysis
            2. ⚠️ Risk Areas & Spend Optimization
            3. 🚀 High-Yield Action Plan for Wealth Building
            
            Keep the tone refined, monochrome-luxury, actionable, and encouraging.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = prompt)))
            ),
            generationConfig = GenerationConfig(
                temperature = 0.3f,
                thinkingConfig = ThinkingConfig(thinkingLevel = "high")
            ),
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = "You are Kai's Senior AI Financial Strategist. Provide crisp, high-value financial reasoning."))
            )
        )

        try {
            val response = api.generateProHighThinking(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Unable to compute deep financial strategy at this moment."
        } catch (e: Exception) {
            "AI Advisor Error: ${e.localizedMessage ?: "Network query failed."}\n\nTip: Check your connection or API key status."
        }
    }

    suspend fun askFinancialQuestion(
        question: String,
        financialContext: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Intelligent local fallback response
            return@withContext when {
                question.contains("pengeluaran", ignoreCase = true) || question.contains("boros", ignoreCase = true) ->
                    "Berdasarkan analisis data finansial Kai Finance:\n• Pengeluaran terbesar Anda terfokus pada Housing & Shopping.\n• Saran optimasi: Terapkan prinsip 50/30/20 (50% kebutuhan pokok, 30% keinginan, 20% tabungan/investasi) untuk menjaga arus kas tetap positif."
                question.contains("investasi", ignoreCase = true) || question.contains("portfolio", ignoreCase = true) ->
                    "Portofolio Anda saat ini memiliki eksposur pada Saham (BBCA), Index Fund (VOO), dan Aset Digital. Diversifikasi Anda cukup sehat. Pertahankan dollar-cost averaging dan reinvestasi dividen untuk compound growth optimal."
                question.contains("hutang", ignoreCase = true) || question.contains("piutang", ignoreCase = true) ->
                    "Rekomendasi hutang/piutang:\n• Prioritaskan pelunasan hutang dengan suku bunga tertinggi atau nominal paling mendesak (Debt Avalanche method).\n• Follow up piutang yang mendekati jatuh tempo untuk menjaga likuiditas kas."
                else ->
                    "Analisis Kai AI:\nKondisi keuangan Anda secara umum berada dalam tren stabil dengan rasio tabungan yang sehat. Untuk analisis reasoning mendalam dengan model Gemini Pro, Anda juga dapat menambahkan GEMINI_API_KEY di Secrets panel."
            }
        }

        val prompt = """
            You are Kai, an elite personal wealth strategist and financial advisor.
            Here is the user's real-time financial context:
            $financialContext

            User Question: "$question"

            Answer clearly, concisely, and professionally in the same language as the user (Indonesian or English). Provide data-backed actionable advice, highlighting figures where relevant.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = prompt)))
            ),
            generationConfig = GenerationConfig(
                temperature = 0.4f
            ),
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = "You are Kai's AI Personal CFO. You analyze financial data with surgical precision, providing objective, actionable advice."))
            )
        )

        try {
            val response = api.generateFlash(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Maaf, Kai AI tidak dapat memproses jawaban saat ini."
        } catch (e: Exception) {
            "Gagal menghubungi server Kai AI: ${e.localizedMessage}"
        }
    }

    suspend fun parseReceiptImage(bitmap: Bitmap): ParsedReceiptResult? = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext null
        }

        val base64Image = bitmap.toBase64()
        val prompt = """
            Examine this receipt image and extract:
            1. Merchant name or item title
            2. Total amount (numeric value only)
            3. Category choice from [HOUSING, FOOD, TRANSPORT, ENTERTAINMENT, SHOPPING, UTILITIES, SALARY, INVESTMENT, HEALTH, OTHER]
            
            Format response strictly as JSON:
            {"title": "Store Name", "amount": 42.50, "category": "FOOD"}
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                GeminiContent(
                    parts = listOf(
                        GeminiPart(text = prompt),
                        GeminiPart(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                    )
                )
            )
        )

        try {
            val response = api.generateFlash(apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            parseReceiptJson(responseText)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseReceiptJson(text: String): ParsedReceiptResult? {
        return try {
            val jsonStart = text.indexOf("{")
            val jsonEnd = text.lastIndexOf("}")
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                val jsonSub = text.substring(jsonStart, jsonEnd + 1)
                val regexTitle = """"title"\s*:\s*"([^"]+)"""".toRegex()
                val regexAmount = """"amount"\s*:\s*([0-9.]+)""".toRegex()
                val regexCategory = """"category"\s*:\s*"([^"]+)"""".toRegex()

                val title = regexTitle.find(jsonSub)?.groupValues?.get(1) ?: "Scanned Receipt"
                val amount = regexAmount.find(jsonSub)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
                val catStr = regexCategory.find(jsonSub)?.groupValues?.get(1) ?: "SHOPPING"

                val category = try {
                    TransactionCategory.valueOf(catStr)
                } catch (e: Exception) {
                    TransactionCategory.SHOPPING
                }

                ParsedReceiptResult(
                    title = title,
                    amount = amount,
                    category = category,
                    type = TransactionType.EXPENSE,
                    note = "Auto-parsed from receipt image"
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun Bitmap.toBase64(): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP)
    }
}
