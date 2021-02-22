[![Download](https://api.bintray.com/packages/christophsturm/maven/filepeek/images/download.svg)](https://bintray.com/christophsturm/maven/filepeek/_latestVersion)
[![CircleCI](https://circleci.com/gh/christophsturm/filepeek/tree/master.svg?style=svg)](https://circleci.com/gh/christophsturm/filepeek/tree/master)

# FilePeek

FilePeek is a library that finds sourcecode to be used in assertion libs and test runners.

Get it from bintray:
```
repositories {
    jcenter() // or
    // maven { url  "https://dl.bintray.com/christophsturm/maven" }
}
dependencies{
    implementation("com.christophsturm:filepeek:0.1.1")
}
```

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

