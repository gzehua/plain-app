import java.util.Properties

gradle.projectsEvaluated {
    val shared = rootProject.project(":shared")
    shared.tasks.register("checkTestTaskIsolation") {
        doLast { shared.tasks.getByName("testAndroidHostTest") }
    }
}

gradle.taskGraph.whenReady {
    val output = System.getProperty("plain.build.plan")
    if (output != null) {
        val app = gradle.rootProject.project(":app")
        val nativeTasks = app.tasks.withType(Exec::class.java).filter { it.name.startsWith("buildPlainRustAndroid") }
        val properties = Properties()
        properties.setProperty("selected", allTasks.filter { it in nativeTasks }.map { it.name }.sorted().joinToString("\n"))
        for (task in nativeTasks) {
            properties.setProperty("${task.name}.command", task.commandLine.joinToString("\n"))
            properties.setProperty("${task.name}.outputs", task.outputs.files.files.map { it.absolutePath }.sorted().joinToString("\n"))
        }
        File(output).outputStream().use { properties.store(it, null) }
    }
}
