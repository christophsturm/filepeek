package filepeek

data class FileInfo(
    val lineNumber: Int,
    val sourceFileName: String,
    val line: String,
    val methodName: String
)

expect class FilePeek(
    ignoredPackages: List<String> = emptyList(),
    sourceRoots: List<String> = listOf("src/test/kotlin", "src/test/java")
){
    fun getCallerFileInfo(): FileInfo
}
