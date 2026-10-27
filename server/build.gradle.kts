import com.google.cloud.tools.jib.gradle.BuildImageTask
import com.google.cloud.tools.jib.gradle.BuildTarTask
import com.google.cloud.tools.jib.gradle.JibExtension
import com.google.cloud.tools.jib.gradle.JibTask
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

plugins {
	kotlin("jvm") version "2.3.21"
	kotlin("plugin.spring") version "2.3.21"
	id("org.springframework.boot") version "4.1.1"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.google.cloud.tools.jib") version "3.5.4"
	jacoco
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-micrometer-tracing-brave")
	implementation("io.micrometer:micrometer-tracing-bridge-brave")

	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("tools.jackson.module:jackson-module-kotlin")

	implementation("org.postgresql:postgresql")
	runtimeOnly("org.flywaydb:flyway-database-postgresql")

	// Testing
	testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jdbc-test")
	testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
	testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:testcontainers-junit-jupiter:2.0.5")
	testImplementation("org.testcontainers:testcontainers-postgresql:2.0.5")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.assertj:assertj-core")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

springBoot {
	buildInfo()
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required = false
		csv.required = true
		html.required = true
	}
}

tasks.jar {
	enabled = false
}

tasks.processResources {
	dependsOn(":web:pnpmBuild")
	from(project(":web").layout.projectDirectory.dir("dist")) {
		into("static")
	}
}

val porta = 9100

val classePrincipal = "br.dev.brunorsch.ledger.server.LedgerApplicationKt"

val registroProducao = providers.gradleProperty("jibRegistro").getOrElse("ghcr.io/brunorsch")

val arquiteturaLocal = when (System.getProperty("os.arch")) {
	"aarch64", "arm64" -> "arm64"
	else -> "amd64"
}

val rotulosImagem = mapOf(
	"org.opencontainers.image.title" to "ledger",
	"org.opencontainers.image.description" to "Ledger - App de controle financeiro pessoal",
	"org.opencontainers.image.source" to "https://github.com/brunorsch/ledger",
	"org.opencontainers.image.version" to project.version.toString(),
)

jib {
	from {
		image = "eclipse-temurin:25-jre-noble"
		platforms {
			platform {
				os = "linux"
				architecture = arquiteturaLocal
			}
		}
	}
	to {
		image = "ledger-server:dev"
	}
	container {
		mainClass = classePrincipal
		ports = listOf("$porta")
		jvmFlags = listOf("-XX:MaxRAMPercentage=75.0")
		labels.putAll(rotulosImagem)
	}
	outputPaths {
		tar = "build/jib-dev.tar"
	}
}

val jibProducao = extensions.create<JibExtension>("jibProducao", project).apply {
	from.image = "gcr.io/distroless/java25-debian13:nonroot"
	to.image = "$registroProducao/ledger-server:prod"
	to.tags = setOf("latest")
	container.mainClass = classePrincipal
	container.ports = listOf("$porta")
	container.user = "65532:65532"
	container.jvmFlags = listOf(
		"-XX:MaxRAMPercentage=75.0",
		"-XX:+ExitOnOutOfMemoryError",
		"-XX:ErrorFile=/tmp/hs_err_pid%p.log",
		"-XX:+HeapDumpOnOutOfMemoryError",
		"-XX:HeapDumpPath=/tmp",
	)
	container.labels.putAll(rotulosImagem)
	outputPaths.tar = "build/jib-prod.tar"
}

val classePrincipalRuntime = the<SourceSetContainer>()[SourceSet.MAIN_SOURCE_SET_NAME].runtimeClasspath
val configuracaoRuntime = configurations.named("runtimeClasspath")

tasks.register<BuildImageTask>("jibProd") {
	group = "jib"
	description = "Publica a imagem de produção no registry."
	setJibExtension(jibProducao)
	dependsOn(configuracaoRuntime)
	dependsOn(classePrincipalRuntime)
}

tasks.register<BuildTarTask>("jibProdTar") {
	group = "jib"
	description = "Gera o tar da imagem de produção, sem precisar de Docker."
	setJibExtension(jibProducao)
	dependsOn(configuracaoRuntime)
	dependsOn(classePrincipalRuntime)
}

val tarefasImagemDev = setOf("jibDev", "jibDevTar", "jibDockerBuild", "jibBuildTar")

gradle.taskGraph.whenReady {
	if (allTasks.any { it.name in tarefasImagemDev } && project.findProperty("viteModo") != "development") {
		throw GradleException("A imagem de dev embute a config de dev do front: rode com -PviteModo=development")
	}
}

tasks.register("jibDev") {
	group = "jib"
	description = "Gera a imagem de dev e carrega no daemon Docker local."
	dependsOn(tasks.named("jibDockerBuild"))
}

tasks.register("jibDevTar") {
	group = "jib"
	description = "Gera o tar da imagem de dev, sem precisar de Docker."
	dependsOn(tasks.named("jibBuildTar"))
}

// O Jib 3.5.4 ainda não suporta configuration cache (https://github.com/GoogleContainerTools/jib/issues/3132).
tasks.withType<JibTask>().configureEach {
	notCompatibleWithConfigurationCache("O plugin Jib ainda não suporta configuration cache.")
}
