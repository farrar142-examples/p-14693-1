plugins {
	java
	id("org.springframework.boot") version "4.1.0-M1"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
	maven { url = uri("https://repo.spring.io/milestone") }
	maven { url = uri("https://repo.spring.io/snapshot") }
	maven {
		name = "Central Portal Snapshots"
		url = uri("https://repo.spring.io/snapshots/central-portal")
	}
}

extra["springAiVersion"] = "2.0.0-SNAPSHOT"

dependencyManagement {
	imports {
		mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	// database
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	// spring ai
	implementation("org.springframework.ai:spring-ai-starter-model-openai")
	implementation("org.springframework.ai:spring-ai-starter-model-ollama")
	// spring ai chat memory & vector store
	implementation("org.springframework.ai:spring-ai-starter-vector-store-chroma")
	implementation("org.springframework.ai:spring-ai-advisors-vector-store")
	implementation("org.springframework.ai:spring-ai-starter-model-chat-memory-repository-jdbc")
	// hibernate-vector for pgvector support
	implementation("org.hibernate.orm:hibernate-vector")
	// json (required for ChromaDB)
	implementation("org.springframework.boot:spring-boot-starter-json")
	compileOnly("org.projectlombok:lombok")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:testcontainers-junit-jupiter")
	testImplementation("org.testcontainers:testcontainers-postgresql")
	testImplementation("org.testcontainers:testcontainers-chromadb")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}