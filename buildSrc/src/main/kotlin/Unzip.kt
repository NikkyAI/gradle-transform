import org.gradle.api.artifacts.transform.InputArtifact
import org.gradle.api.artifacts.transform.TransformAction
import org.gradle.api.artifacts.transform.TransformOutputs
import org.gradle.api.artifacts.transform.TransformParameters
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

abstract class Unzip : TransformAction<TransformParameters.None> {          // (1)
    @get:InputArtifact                                                      // (2)
    abstract val inputArtifact: Provider<FileSystemLocation>

    private val BUFFER_SIZE = 4096

    override
    fun transform(outputs: TransformOutputs) {
        val input = inputArtifact.get().asFile
        val unzipDir = outputs.dir(input.nameWithoutExtension + "-classes")                              // (3)
        unzipTo(input, unzipDir)                                            // (4)
    }

    private fun unzipTo(zipFile: File, unzipDir: File) {
        System.err.println("unzipping: $zipFile into $unzipDir")
        require(zipFile.exists()) { "$zipFile does not exist" }
        require(zipFile.isFile) { "$zipFile not not a file" }
        val destDir = unzipDir.absoluteFile
        if (!destDir.exists()) {
            destDir.mkdir()
        }
        // TODO: error when file is not a zipfile
        val zipIn = ZipInputStream(FileInputStream(zipFile))
        var entry: ZipEntry? = zipIn.nextEntry
        // iterates over entries in the zip file
        while (entry != null) {
            val filePath = destDir.resolve(entry.name).path
            if (!entry.isDirectory) {
                // if the entry is a file, extracts it
                File(filePath).parentFile.mkdirs()
                extractFile(zipIn, filePath)
            } else {
                // if the entry is a directory, make the directory
                val dir = File(filePath)
                dir.mkdir()
            }
            zipIn.closeEntry()
            entry = zipIn.nextEntry
        }
        zipIn.close()
    }

    /**
     * Extracts a zip entry (file entry)
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun extractFile(zipIn: ZipInputStream, filePath: String) {
        val bos = BufferedOutputStream(FileOutputStream(filePath))
        val bytesIn = ByteArray(BUFFER_SIZE)
        var read: Int
        while (true) {
            read = zipIn.read(bytesIn)
            if (read < 0) break
            bos.write(bytesIn, 0, read)
        }
        bos.close()
    }
}