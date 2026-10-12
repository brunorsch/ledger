import com.github.gradle.node.pnpm.task.PnpmTask

plugins {
    id("com.github.node-gradle.node") version "7.1.0"
}

node {
    download.set(false)
    pnpmVersion.set("12.11.2")
}

val pnpmBuild = tasks.register<PnpmTask>("pnpmBuild") {
    dependsOn(tasks.named("pnpmInstall"))
    pnpmCommand.set(listOf("run", "build"))
    inputs.dir("src")
    inputs.files("package.json", "pnpm-lock.yaml", "index.html", "vite.config.ts")
    outputs.dir("dist")
}
