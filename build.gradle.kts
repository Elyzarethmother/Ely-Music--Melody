// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.secrets) apply false
  alias(libs.plugins.google.services) apply false
}

// Ensure debug.keystore exists before build tasks execute (e.g., in CI or clean git clones)
val debugKeystore = file("debug.keystore")
val debugKeystoreB64 = file("debug.keystore.base64")
if (!debugKeystore.exists() || debugKeystore.length() == 0L) {
  if (debugKeystoreB64.exists() && debugKeystoreB64.length() > 0L) {
    try {
      val decoded = java.util.Base64.getMimeDecoder().decode(debugKeystoreB64.readText())
      debugKeystore.writeBytes(decoded)
    } catch (_: Exception) {}
  }
  if (!debugKeystore.exists() || debugKeystore.length() == 0L) {
    try {
      ProcessBuilder(
        "keytool", "-genkey", "-v", "-keystore", debugKeystore.absolutePath,
        "-storepass", "android", "-alias", "androiddebugkey", "-keypass", "android",
        "-keyalg", "RSA", "-keysize", "2048", "-validity", "10000",
        "-dname", "CN=Android Debug,O=Android,C=US"
      ).start().waitFor()
    } catch (_: Exception) {}
  }
}

