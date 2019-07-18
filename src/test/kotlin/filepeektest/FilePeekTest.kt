package filepeektest

import filepeek.FileInfo
import filepeek.FilePeek
import filepeek.mapMethod
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.endsWith
import strikt.assertions.isEqualTo

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
