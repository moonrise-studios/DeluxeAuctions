import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.gradleup.shadow") version "9.4.1"
}

group = "me.sedattr.deluxeauctions"
version = "4.3"

repositories {
    mavenCentral()
    maven("https://repo.nightexpressdev.com/releases")
    maven("https://repo.skriptlang.org/releases")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://jitpack.io")
    maven("https://repo.techscode.com/repository/techscode-apis/")
    maven("https://repo.auxilor.io/repository/maven-public/")
    maven("https://repo.rosewooddev.io/repository/public/")
    maven("https://ci.frostcast.net/plugin/repository/everything")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    compileOnly("su.nightexpress.coinsengine:CoinsEngine:2.5.0")
    compileOnly("com.github.SkriptLang:Skript:2.12.1")
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.arcaniax:HeadDatabase-API:1.3.2")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude(group = "org.bukkit", module = "bukkit")
    }
    compileOnly("com.github.angeschossen:LandsAPI:7.10.13")
    compileOnly("com.gitlab.ruany:LiteBansAPI:0.5.0")
    compileOnly("com.github.DevLeoko:AdvancedBan:v2.3.0") {
        exclude(group = "org.bstats")
    }
    compileOnly("me.TechsCode:UltraEconomyAPI:1.1.2")
    compileOnly("me.confuser.banmanager:BanManagerBukkit:7.9.0")
    compileOnly("org.black_ixx:playerpoints:3.3.0")

    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")

    implementation("org.incendo:cloud-paper:2.0.0-beta.10")
    implementation("org.incendo:cloud-annotations:2.0.0")
    implementation("net.wesjd:anvilgui:1.10.11-SNAPSHOT")
    implementation("de.rapha149.signgui:signgui:2.5.4")
    implementation("net.objecthunter:exp4j:0.4.8")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.withType<ShadowJar>().configureEach {
    archiveClassifier.set("")

    dependencies {
        include(dependency("org.incendo:.*:.*"))
        include(dependency("io.leangen.geantyref:.*:.*"))
        include(dependency("com.mojang:brigadier:.*"))
        include(dependency("net.wesjd:anvilgui:.*"))
        include(dependency("de.rapha149.signgui:signgui:.*"))
        include(dependency("net.objecthunter:exp4j:.*"))
    }

    relocate("org.incendo.cloud", "me.sedattr.deluxeauctions.cloud")
    relocate("net.wesjd.anvilgui", "me.sedattr.deluxeauctions.anvilgui")
    relocate("de.rapha149.signgui", "me.sedattr.deluxeauctions.signgui")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.test {
    useJUnitPlatform()
}
