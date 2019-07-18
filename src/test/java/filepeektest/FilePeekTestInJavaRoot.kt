package filepeektest

import filepeek.FilePeek
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class FilePeekTestInJavaRoot {
    @Test
    fun `finds classes that have a different name than the file they are in`() {
        val filePeek = FilePeek(listOf("filepeek."))
        expectThat(filePeek.getCallerFileInfo())
            .get { line }
            .isEqualTo("expectThat(filePeek.getCallerFileInfo())")
    }
}
