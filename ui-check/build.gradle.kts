plugins {
    kotlin("jvm")
    application
}

kotlin {
    jvmToolchain(17)
    sourceSets.getByName("test").kotlin.setSrcDirs(listOf(rootProject.file("tests/ui-check/test/kotlin")))
}
sourceSets.getByName("test").resources.setSrcDirs(listOf(rootProject.file("tests/ui-check/test/resources")))
dependencies {
    implementation(libs.kotlin.compiler.embeddable)
    testImplementation(kotlin("test-junit"))
}
application { mainClass.set("com.ismartcoding.uicheck.MainKt") }

tasks.register<JavaExec>("checkAndroidCollections") {
    group = "verification"
    val sources = rootProject.layout.projectDirectory.dir("shared/src/androidMain")
    inputs.dir(sources)
    classpath = sourceSets.getByName("main").runtimeClasspath
    mainClass.set("com.ismartcoding.uicheck.MainKt")
    args("--java21", sources.asFile.absolutePath)
}
