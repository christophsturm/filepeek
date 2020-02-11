package filepeek

import java.io.File

data class FileInfo(
    val lineNumber: Int,
    val sourceFileName: String,
    val line: String,
    val methodName: String
)

private val FS = File.separator

class FilePeek(
    private val ignoredPackages: List<String> = emptyList(),
    val sourceRoots: List<String> = listOf("src${FS}test${FS}kotlin", "src${FS}test${FS}java")
) {

    fun getCallerFileInfo(
    ): FileInfo {
        val stackTrace = RuntimeException().stackTrace

        val callerStackTraceElement = stackTrace.first { el ->
            ignoredPackages
                .none { el.className.startsWith(it) }
        }
        val className = callerStackTraceElement.className.substringBefore('$')
        val clazz = javaClass.classLoader.loadClass(className)!!
        val classFilePath = File(clazz.protectionDomain.codeSource.location.path)
            .absolutePath

        val buildDir = when {
            classFilePath.contains("${FS}out${FS}") -> "out${FS}test${FS}classes" // running inside IDEA
            classFilePath.contains("build${FS}classes${FS}java") -> "build${FS}classes${FS}java${FS}test" // gradle 4.x java source
            classFilePath.contains("build${FS}classes${FS}kotlin") -> "build${FS}classes${FS}kotlin${FS}test" // gradle 4.x kotlin sources
            classFilePath.contains("target${FS}classes") -> "target${FS}classes" // maven
            else -> "build${FS}classes${FS}test" // older gradle
        }

        val sourceFileCandidates = this.sourceRoots
            .map { sourceRoot ->
                val sourceFileWithoutExtension =
                    classFilePath.replace(buildDir, sourceRoot)
                        .plus("${FS}" + className.replace(".", "${FS}"))

                File(sourceFileWithoutExtension).parentFile
                    .resolve(callerStackTraceElement.fileName!!)
            }
        val sourceFile = sourceFileCandidates.singleOrNull(File::exists) ?: throw SourceFileNotFoundException(
            classFilePath,
            className,
            sourceFileCandidates
        )

        val callerLine = sourceFile.bufferedReader().useLines { lines ->
            var braceDelta = 0
            lines.drop(callerStackTraceElement.lineNumber - 1)
                .takeWhileInclusive { line ->
                    val openBraces = line.count { it == '{' }
                    val closeBraces = line.count { it == '}' }
                    braceDelta += openBraces - closeBraces
                    braceDelta != 0
                }.map { it.trim() }.joinToString(separator = "")
        }

        return FileInfo(
            callerStackTraceElement.lineNumber,
            sourceFileName = sourceFile.absolutePath,
            line = callerLine.trim(),
            methodName = callerStackTraceElement.methodName

        )
    }
}

internal fun <T> Sequence<T>.takeWhileInclusive(pred: (T) -> Boolean): Sequence<T> {
    var shouldContinue = true
    return takeWhile {
        val result = shouldContinue
        shouldContinue = pred(it)
        result
    }
}

class SourceFileNotFoundException(classFilePath: String, className: String, candidates: List<File>) :
    java.lang.RuntimeException("did not find source file for class $className loaded from $classFilePath. tried: ${candidates.joinToString { it.path }}") {
}

