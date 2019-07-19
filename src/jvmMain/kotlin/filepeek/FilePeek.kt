package filepeek

import java.io.File

actual class FilePeek actual constructor(
    private val ignoredPackages: List<String> ,
    private val sourceRoots: Sequence<String>
) {

    actual fun getCallerFileInfo(
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
            //TODO most likely these two lines need a fix
            classFilePath.contains("build/classes/kotlin/jvm") -> "build/classes/kotlin/jvm/test" // gradle 4.x kotlin multi-platform sources jvm
            classFilePath.contains("build/classes/kotlin/js") -> "build/classes/kotlin/js/test" // gradle 4.x kotlin multi-platform sources js

            classFilePath.contains("build/classes/kotlin/test") -> "build/classes/kotlin/test" // gradle 4.x kotlin sources
            classFilePath.contains("target/classes") -> "target/classes" // maven
            else -> "build/classes/test" // older gradle
        }

        val sourceFileCandidates = this.sourceRoots
            .map { sourceRoot ->
                val sourceFileWithoutExtension =
                    classFilePath.replace(buildDir, sourceRoot)
                        .plus("/" + className.replace(".", "/"))

                File(sourceFileWithoutExtension).parentFile
                    .resolve(callerStackTraceElement.fileName!!)
            }
        val sourceFile = sourceFileCandidates.single(File::exists)

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