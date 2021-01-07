package filepeek

import failfast.Suite
import filepeektest.FilePeekTest
import filepeektest.FilePeekTestWithDifferentNameThanItsFile

fun main() {
    Suite.fromContexts(
        listOf(
            LambdaBodyTest.context,
            FilePeekTest.context,
            FilePeekTestWithDifferentNameThanItsFile.context
        )
    ).run().check()
}
