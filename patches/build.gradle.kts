group = "app.template"

patches {
    // TODO: Update this section with your project details.
    about {
        name = "Telegram 12.10.5 Morphe Patches"
        description = "Morphe patches for Telegram Android 12.10.5"
        source = "https://github.com/cingxcong/morphe.patches.telegram.git"
        author = "cingxcong"
        contact = "na"
        website = "https://telegram.org"
        license = "GPLv3"
    }
}

// Separate configuration so gson is available at runtime for the
// generatePatchesList task but never bundled into the APK.
val patchListGeneratorClasspath = configurations.create("patchListGeneratorClasspath")

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
    }

    // Used by gradle-semantic-release-plugin.
    publish {
        dependsOn("generatePatchesList")
    }
}
