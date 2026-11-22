import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val localProperties = Properties().apply {
    val propertiesFile = rootProject.layout.projectDirectory.file("local.properties")
    providers.fileContents(propertiesFile).asText.orNull?.reader()?.use { load(it) }
}

fun javaStringLiteral(value: String): String = buildString {
    append('"')
    for (character in value) {
        append(
            when (character) {
                '\\' -> "\\\\"
                '"' -> "\\\""
                '\n' -> "\\n"
                '\r' -> "\\r"
                '\t' -> "\\t"
                else -> character.toString()
            }
        )
    }
    append('"')
}

android {
    namespace = "com.posterm.mobiledashboard"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.posterm.mobiledashboard"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "PAYMENT_API_URL", "\"\"")
        buildConfigField("String", "PAYMENT_API_TOKEN", "\"\"")
        buildConfigField("boolean", "TRANSACTION_DEMO_MODE", "false")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            buildConfigField(
                "String", "PAYMENT_API_URL",
                javaStringLiteral(localProperties.getProperty("payment.api.url", "").trim())
            )
            buildConfigField(
                "String", "PAYMENT_API_TOKEN",
                javaStringLiteral(localProperties.getProperty("payment.api.token", "").trim())
            )
            buildConfigField(
                "boolean", "TRANSACTION_DEMO_MODE",
                localProperties.getProperty("payment.demo", "false").trim().toBoolean().toString()
            )
        }
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.recyclerview)
    implementation(libs.swiperefreshlayout)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    testImplementation(libs.junit)
    testImplementation(libs.arch.core.testing)
    testImplementation(libs.okhttp.mockwebserver)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
