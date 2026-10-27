import com.github.gradle.node.pnpm.task.PnpmTask

plugins {
    id("com.github.node-gradle.node") version "7.1.0"
}

node {
    download.set(false)
    pnpmVersion.set("12.11.2")
}

// Modo do Vite: "production" embute a config de prod (.env.production),
// "development" embute a config de dev (.env). jibDev passa -PviteModo=development.
val modoVite = rootProject.findProperty("viteModo") as String? ?: "production"

val pnpmBuild = tasks.register<PnpmTask>("pnpmBuild") {
    dependsOn(tasks.named("pnpmInstall"))
    pnpmCommand.set(listOf("run", "build", "--mode", modoVite))
    inputs.dir("src")
    inputs.files("package.json", "pnpm-lock.yaml", "index.html", "vite.config.ts")
    inputs.files(fileTree(projectDir).matching { include(".env", ".env.production") })
    inputs.property("viteModo", modoVite)
    outputs.dir("dist")
}
