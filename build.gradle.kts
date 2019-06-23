import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.3.31"
}

group = "moe.nikky"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val artifactType = Attribute.of("artifactType", String::class.java)
val minify = Attribute.of("minify", Boolean::class.javaObjectType)
//val remapped = Attribute.of("remapped", Boolean::class.javaObjectType)


//val unzipConfiguration by configurations.creating {
//    configurations.implementation.get().extendsFrom(this)
////    isCanBeResolved = true
//        attributes {
//            attribute(artifactType, "java-classes-directory")
//            attributes.attribute(minify, true) // (3)
//        }
//}

/**
 * adds a new attribute and sets defaults for all artifacts that are jars
 */
dependencies {
    attributesSchema {
        attribute(minify)                      // (1)
    }
    artifactTypes.getByName("jar") {
        attributes.attribute(minify, false)    // (2)
    }
}

/**
 *  applies configuration to all resolvable dependencies
 */

//configurations.all {
//    afterEvaluate {
//        if (isCanBeResolved) {
//            attributes.attribute(minify, true) // (3)
////            attributes.attribute(artifactType, "java-classes-directory")
//        }
//    }
//}

/**
 *  this is used to only minify guava, see Minify.kt line 17
 */

val keepPatterns = mapOf(
    "guava" to setOf(
        "com.google.common.base.Optional",
        "com.google.common.base.AbstractIterator"
    )
)

dependencies {
    registerTransform(Minify::class) {
        from.attribute(minify, false).attribute(artifactType, "jar")
        to.attribute(minify, true).attribute(artifactType, "jar")

        parameters {
            keepClassesByArtifact = keepPatterns
        }
    }
}

dependencies {
    registerTransform(Unzip::class) {
        from.attribute(artifactType, "jar")
        to.attribute(artifactType, "java-classes-directory")
    }
}

/**
 *  wraps applying attributes in a function
 */
fun unzip(dep: ExternalModuleDependency) = dep.apply {
     attributes {
        attribute(artifactType, "java-classes-directory")
    }
}
fun minfiy(dep: ExternalModuleDependency) = dep.apply {
    attributes {
        attribute(minify, true)
    }
}
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    minfiy(unzip(implementation("com.google.guava", "guava:27.1-jre")))
    unzip(testCompile("junit", "junit", "4.12"))

    /**
     * apply attributes manually
     */
//    implementation("com.google.guava:guava:27.1-jre") {
//        attributes {
//            attribute(artifactType, "java-classes-directory")
//            attributes.attribute(minify, true) // (3)
//        }
//    }

}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}