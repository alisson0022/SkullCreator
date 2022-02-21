plugins {
    java
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://libraries.minecraft.net/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.18-R0.1-SNAPSHOT")
    compileOnly("com.mojang:authlib:1.5.21")
}

group = "me.alissonlopes"
version = "1.0.0"
description = "skullcreator"
java.sourceCompatibility = JavaVersion.VERSION_1_8