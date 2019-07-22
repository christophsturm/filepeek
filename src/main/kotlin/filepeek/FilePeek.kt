package filepeek

import java.io.File

data class FileInfo(
    val lineNumber: Int,
    val sourceFileName: String,
    val line: String,
    val methodName: String
)

class FilePeek(
    private val ignoredPackages: List<String> = emptyList(),
    val sourceRoots: Sequence<String> = sequenceOf("src/test/kotlin", "src/test/java")
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
            classFilePath.contains("/out/") -> "out/test/classes" // running inside IDEA
            classFilePath.contains("build/classes/java") -> "build/classes/java/test" // gradle 4.x java source
            classFilePath.contains("build/classes/kotlin") -> "build/classes/kotlin/test" // gradle 4.x kotlin sources
            classFilePath.contains("target/classes") -> "target/classes" // maven
            else -> "build/classes/test" // older gradle
        }

        val sourceFileCandidates = this.sourceRoots.toList()
            .map { sourceRoot ->
                val sourceFileWithoutExtension =
                    classFilePath.replace(buildDir, sourceRoot)
                        .plus("/" + className.replace(".", "/"))

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

