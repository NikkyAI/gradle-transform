import org.gradle.api.artifacts.transform.InputArtifact
import org.gradle.api.artifacts.transform.TransformAction
import org.gradle.api.artifacts.transform.TransformOutputs
import org.gradle.api.artifacts.transform.TransformParameters
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import java.io.File

abstract class Remap : TransformAction<Remap.Parameters> {   // (1)
    interface Parameters : TransformParameters {               // (2)
        @get:Input
        var keepClassesByArtifact: Map<String, Set<String>>

    }

    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    @get:InputArtifact
    abstract val inputArtifact: Provider<FileSystemLocation>

    override
    fun transform(outputs: TransformOutputs) {
        val fileName = inputArtifact.get().asFile.name
        for (entry in parameters.keepClassesByArtifact) {      // (3)
            if (fileName.startsWith(entry.key)) {
                val nameWithoutExtension = fileName.substring(0, fileName.length - 4)
                minify(inputArtifact.get().asFile, entry.value, outputs.file("${nameWithoutExtension}-min.jar"))
                return
            }
        }
        println("Nothing to minify - using ${fileName} unchanged")
        outputs.file(inputArtifact)                            // (4)
    }

    private fun minify(artifact: File, keepClasses: Set<String>, jarFile: File) {
        println("Remapping ${artifact.name}")
        // Implementation ...
        artifact.copyTo(jarFile)
    }
}