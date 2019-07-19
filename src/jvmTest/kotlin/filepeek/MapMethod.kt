package filepeek

fun mapMethod(@Suppress("UNUSED_PARAMETER") block: () -> Unit) =
    FilePeek(listOf("filepeek.")).getCallerFileInfo()
