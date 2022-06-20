plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    id("org.jetbrains.dokka")
    id("com.diffplug.spotless")
    id("com.gradle.plugin-publish")
}

gradlePlugin {
    plugins.register("packagingPlugin") {
        id = "$RELEASE_GROUP.packaging"
        implementationClass = "$id.PackagingPlugin"
        displayName = "Packaging Plugin"
        description = RELEASE_DESCRIPTION
    }
    testSourceSets(sourceSets.test.get())
}

spotless.kotlin { ktlint() }

pluginBundle {
    website = RELEASE_URL
    vcsUrl = "$RELEASE_URL.git"
    description = RELEASE_DESCRIPTION
    tags = listOf("packaging", "jpackage", "native", "installer", "bundle")
}

dependencies {
    implementation(libs.osdetector)
    testImplementation(gradleTestKit())
    testImplementation(testLibs.kotlin.junit)
}

tasks.dokkaHtml {
    outputDirectory.set(buildDir.resolve("dokka/dokka"))
}
