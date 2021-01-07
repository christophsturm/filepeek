package filepeektest

import failfast.describe
import filepeek.FileInfo
import filepeek.FilePeek
import filepeek.SourceFileNotFoundException
import filepeek.mapMethod
import strikt.api.expectCatching
import strikt.api.expectThat
import strikt.assertions.endsWith
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isFailure
import strikt.assertions.isNotNull
import strikt.assertions.startsWith
import java.io.File

private val FS = File.separator

object FilePeekTest {
    val context = describe(FilePeek::class) {
        val fileName = "src${FS}test${FS}kotlin${FS}filepeektest${FS}FilePeekTest.kt"

        val filePeek = FilePeek(listOf("filepeek."))
        it("can get FileInfo") {
            val fileInfo = filePeek.getCallerFileInfo()

            expectThat(fileInfo) {
                get(FileInfo::sourceFileName)
                    .endsWith(fileName)
                get(FileInfo::line)
                    .isEqualTo("""val fileInfo = filePeek.getCallerFileInfo()""")
            }
        }

        it("can get FileInfo for a block") {
            val fileInfo = { filePeek.getCallerFileInfo() }()

            expectThat(fileInfo) {
                get(FileInfo::sourceFileName)
                    .endsWith(fileName)
                get(FileInfo::line)
                    .isEqualTo("""val fileInfo = { filePeek.getCallerFileInfo() }()""")
            }
        }


        it("throws a useful exception when the file is not found") {
            // this filepeek instance excludes the tests so it will not find the source
            val otherFilePeek = FilePeek(listOf("filepeek."), listOf())
            expectCatching {
                otherFilePeek.getCallerFileInfo()
            }.isFailure().isA<SourceFileNotFoundException>().get(Throwable::message).isNotNull().and {
                startsWith("did not find source file for class filepeektest.FilePeekTest")
            }

        }


        it("can get lambda body even when it contains multiple blocks") {
// @formatter:off (the next block is formatted uneven to check that brackets are counted correctly
            val fileInfo = mapMethod {
                listOf(1).map { listOf(1).map {it}}
            }
            val fileInfo2 = mapMethod {listOf(1).map { listOf(1).map {it}
            }
            }

// @formatter:on
            expectThat(fileInfo).get(FileInfo::line)
                .isEqualTo("val fileInfo = mapMethod {listOf(1).map { listOf(1).map {it}}}")
            expectThat(fileInfo2).get(FileInfo::line)
                .isEqualTo("val fileInfo2 = mapMethod {listOf(1).map { listOf(1).map {it}}}")
        }

    }
}


object FilePeekTestWithDifferentNameThanItsFile {
    val context = describe("test with different file") {
        test("finds classes that have a different name than the file they are in") {
            val filePeek = FilePeek(listOf("filepeek."))
            expectThat(filePeek.getCallerFileInfo())
                .get { line }
                .isEqualTo("expectThat(filePeek.getCallerFileInfo())")
        }

    }
}
