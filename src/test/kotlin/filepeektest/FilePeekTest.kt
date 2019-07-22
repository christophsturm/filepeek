package filepeektest

import filepeek.FileInfo
import filepeek.FilePeek
import filepeek.SourceFileNotFoundException
import filepeek.mapMethod
import org.junit.jupiter.api.Test
import strikt.api.expectCatching
import strikt.api.expectThat
import strikt.assertions.endsWith
import strikt.assertions.failed
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.startsWith

class FilePeekTest {
    private val fileName = "src/test/kotlin/filepeektest/FilePeekTest.kt"

    private val filePeek = FilePeek(listOf("filepeek."))
    @Test
    fun `can get FileInfo`() {
        val fileInfo = filePeek.getCallerFileInfo()

        expectThat(fileInfo) {
            get(FileInfo::sourceFileName)
                .endsWith(fileName)
            get(FileInfo::line)
                .isEqualTo("""val fileInfo = filePeek.getCallerFileInfo()""")
            get(FileInfo::methodName)
                .isEqualTo("""can get FileInfo""")
        }
    }

    @Test
    fun `can get FileInfo for a block`() {
        val fileInfo = { filePeek.getCallerFileInfo() }()

        expectThat(fileInfo) {
            get(FileInfo::sourceFileName)
                .endsWith(fileName)
            get(FileInfo::line)
                .isEqualTo("""val fileInfo = { filePeek.getCallerFileInfo() }()""")
        }
    }

    @Test
    fun `throws a useful exception when the file is not found`() {
        // this filepeek instance excludes the tests so it will not find the source
        val filePeek = FilePeek(listOf("filepeek."), sequenceOf())
        expectCatching {
            filePeek.getCallerFileInfo()
        }.failed().isA<SourceFileNotFoundException>().get(Throwable::message).isNotNull().and {
            startsWith("did not find source file for class filepeektest.FilePeekTest")
        }

    }

    @Test
    fun `can get block body even when it contains multiple `() {

        val fileInfo = mapMethod {
            /* LOL! I'm a block body*/
            listOf(1, 2, 3).map { it }
        }

        expectThat(fileInfo).get(FileInfo::line)
            .isEqualTo("val fileInfo = mapMethod {/* LOL! I'm a block body*/listOf(1, 2, 3).map { it }}")
    }
}


class FilePeekTestWithDifferentNameThanItsFile {
    @Test
    fun `finds classes that have a different name than the file they are in`() {
        val filePeek = FilePeek(listOf("filepeek."))
        expectThat(filePeek.getCallerFileInfo())
            .get { line }
            .isEqualTo("expectThat(filePeek.getCallerFileInfo())")
    }
}
