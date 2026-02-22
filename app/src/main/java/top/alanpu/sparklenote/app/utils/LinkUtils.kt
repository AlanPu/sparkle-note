package top.alanpu.sparklenote.app.utils

/**
 * Utility class for detecting and extracting links from text content.
 * Supports HTTP/HTTPS URLs and provides helper functions for link management.
 */
object LinkUtils {
    
    // 匹配HTTP/HTTPS链接的正则表达式
    // 支持常见的URL格式，包括子域名、路径、查询参数等
    private val URL_PATTERN = Regex(
        "(https?://[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)+([a-zA-Z0-9-._~:/?#\\[\\]@!$\u0026'()*+,;=%])*)"
    )
    
    /**
     * 从文本中提取所有链接
     * @param text 要检测的文本
     * @return 链接列表，如果没有找到则返回空列表
     */
    fun extractLinks(text: String): List<String> {
        return URL_PATTERN.findAll(text).map { it.value }.toList()
    }
    
    /**
     * 检测文本中是否包含链接
     * @param text 要检测的文本
     * @return 如果包含链接返回true，否则返回false
     */
    fun hasLinks(text: String): Boolean {
        return URL_PATTERN.containsMatchIn(text)
    }
    
    /**
     * 获取文本中的第一个链接
     * @param text 要检测的文本
     * @return 第一个链接，如果没有找到则返回null
     */
    fun getFirstLink(text: String): String? {
        return URL_PATTERN.find(text)?.value
    }
    
    /**
     * 验证链接是否有效
     * @param url 要验证的链接
     * @return 如果链接格式有效返回true，否则返回false
     */
    fun isValidUrl(url: String): Boolean {
        return try {
            val uri = android.net.Uri.parse(url)
            uri.scheme != null && (uri.scheme == "http" || uri.scheme == "https")
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 格式化链接显示文本
     * @param url 原始链接
     * @param maxLength 最大显示长度
     * @return 格式化后的链接文本
     */
    fun formatLinkForDisplay(url: String, maxLength: Int = 30): String {
        return if (url.length > maxLength) {
            "${url.take(maxLength)}..."
        } else {
            url
        }
    }
}