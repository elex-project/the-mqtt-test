plugins {
	java
	application
}

group = "kr.co.the_e"
version = "1.0.0"

application {
	mainClass.set("kr.co.the_e.mqtt.App");
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(group = "org.eclipse.paho", name = "org.eclipse.paho.client.mqttv3", version = "1.2.5")
	implementation(group = "com.google.guava", name = "guava", version = "30.1-jre")

	testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

java {
	sourceCompatibility = org.gradle.api.JavaVersion.VERSION_11
	targetCompatibility = org.gradle.api.JavaVersion.VERSION_11
}

tasks.compileJava {
	options.encoding = "UTF-8"
}

tasks.compileTestJava {
	options.encoding = "UTF-8"
}

tasks.jar{
	manifest {
		attributes(mapOf(
			"Implementation-Title" to project.name,
			"Implementation-Version" to project.version,
			"Implementation-Vendor" to "THE co.,ltd.",
			"Main-Class" to application.mainClass
		))
	}
}

tasks.test {
	useJUnitPlatform()
}
