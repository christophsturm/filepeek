package filepeek

actual class FilePeek actual constructor(
    ignoredPackages: List<String>,
    sourceRoots: Sequence<String>
) {
    actual fun getCallerFileInfo(): FileInfo {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}