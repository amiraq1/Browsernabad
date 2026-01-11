package com.nabdh.browser.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object SearchRepository {

    // استخدام API الخاص بمتصفح فايرفوكس لأنه يعطي نتائج JSON نظيفة
    private const val SUGGEST_URL = "http://suggestqueries.google.com/complete/search?client=firefox&q="

    suspend fun getSuggestions(query: String): List<String> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()

        val resultList = mutableListOf<String>()
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = URL(SUGGEST_URL + encodedQuery)
            
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 1000 // مهلة قصيرة جداً للسرعة
            
            // قراءة البيانات
            val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
            
            // تحليل JSON: يأتي بصيغة ["query", ["sugg1", "sugg2", ...]]
            val jsonArray = JSONArray(jsonString)
            val suggestionsArray = jsonArray.getJSONArray(1)

            for (i in 0 until suggestionsArray.length()) {
                resultList.add(suggestionsArray.getString(i))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext resultList
    }
}
