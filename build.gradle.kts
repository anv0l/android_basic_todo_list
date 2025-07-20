// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
//    kotlin("kapt") version "2.1.21"
//    alias(libs.plugins.android.application) apply false
//    alias(libs.plugins.kotlin.android) apply false
    id("com.android.application") version "8.6.1" apply false
    id("org.jetbrains.kotlin.android") version "2.1.10" apply false
    id("com.google.dagger.hilt.android") version "2.56.2" apply false
    id("com.google.devtools.ksp") version "2.1.10-1.0.30" apply false
    alias(libs.plugins.room) apply false
    id("org.jetbrains.kotlin.plugin.serialization") version("1.9.0") apply false
}
