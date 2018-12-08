plugins {
    java
}

group = "de"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testCompile("junit", "junit", "4.12")
    testCompile("org.mockito", "mockito-core", "2.23.4")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}


task("runBlockchainNetwork", JavaExec::class) {
    group = "CustomTasks"
    main = "blockchain.BlockchainNetwork"
    classpath = sourceSets["main"].runtimeClasspath
}
