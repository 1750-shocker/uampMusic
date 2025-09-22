package com.wzh.uampmusic.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * 网络连接测试工具
 */
object NetworkTestHelper {
    
    private const val TAG = "NetworkTest"
    private const val UAMP_CATALOG_URL = "https://storage.googleapis.com/uamp/catalog.json"
    
    /**
     * 检查基本网络连接
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
               capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
    
    /**
     * 测试UAMP数据源连接
     */
    suspend fun testUampDataSource(): TestResult = withContext(Dispatchers.IO) {
        try {
            val url = URL(UAMP_CATALOG_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000 // 10秒超时
            connection.readTimeout = 10000
            
            val responseCode = connection.responseCode
            val responseMessage = connection.responseMessage
            
            Log.d(TAG, "UAMP数据源测试 - 响应码: $responseCode, 消息: $responseMessage")
            
            when (responseCode) {
                200 -> TestResult.Success("连接成功")
                else -> TestResult.Error("HTTP错误: $responseCode - $responseMessage")
            }
        } catch (e: IOException) {
            Log.e(TAG, "网络连接测试失败", e)
            TestResult.Error("网络连接失败: ${e.message}")
        }
    }
    
    /**
     * 测试DNS解析
     */
    suspend fun testDnsResolution(): TestResult = withContext(Dispatchers.IO) {
        try {
            val url = URL(UAMP_CATALOG_URL)
            val host = url.host
            val address = java.net.InetAddress.getByName(host)
            Log.d(TAG, "DNS解析成功: $host -> ${address.hostAddress}")
            TestResult.Success("DNS解析成功: $host -> ${address.hostAddress}")
        } catch (e: Exception) {
            Log.e(TAG, "DNS解析失败", e)
            TestResult.Error("DNS解析失败: ${e.message}")
        }
    }
    
    /**
     * 综合网络诊断
     */
    suspend fun runNetworkDiagnostics(context: Context): DiagnosticResult {
        val results = mutableListOf<String>()
        var hasError = false
        
        // 1. 基本网络检查
        if (isNetworkAvailable(context)) {
            results.add("✅ 基本网络连接正常")
        } else {
            results.add("❌ 无网络连接")
            hasError = true
        }
        
        // 2. DNS解析测试
        when (val dnsResult = testDnsResolution()) {
            is TestResult.Success -> results.add("✅ ${dnsResult.message}")
            is TestResult.Error -> {
                results.add("❌ ${dnsResult.message}")
                hasError = true
            }
        }
        
        // 3. UAMP数据源测试
        when (val sourceResult = testUampDataSource()) {
            is TestResult.Success -> results.add("✅ ${sourceResult.message}")
            is TestResult.Error -> {
                results.add("❌ ${sourceResult.message}")
                hasError = true
            }
        }
        
        return DiagnosticResult(
            success = !hasError,
            details = results
        )
    }
    
    sealed class TestResult {
        data class Success(val message: String) : TestResult()
        data class Error(val message: String) : TestResult()
    }
    
    data class DiagnosticResult(
        val success: Boolean,
        val details: List<String>
    )
}