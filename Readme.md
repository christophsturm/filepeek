# FilePeek

FilePeek is a library that finds sourcecode to be used in assertion libs and test runners.

I originally wrote it for my poc assertion lib asserto, then integrated it into strikt and now extracted it for use in atrium.

example use:

```
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
```
multiline calls work too, lines are read and joined until the method call is complete.

there is also a utility class that extracts a lambda body from a method call
```
    @Test
    fun `extracts the body`() {
        Assertions.assertEquals(
            "name",
            LambdaBody("get", """get { name }.isEqualTo("Ziggy")""").body
        )
    }
```

[![CircleCI](https://circleci.com/gh/christophsturm/filepeek/tree/master.svg?style=svg)](https://circleci.com/gh/christophsturm/filepeek/tree/master)
