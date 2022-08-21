package `fun`.hydd.cdda.item_browser_be.util

import java.security.MessageDigest

fun getStringHash(string: String): String {
  val messageDigest = MessageDigest.getInstance("SHA-256")
  val hash = messageDigest.digest(string.toByteArray())
  return hash.fold("") { str, it -> str + "%02x".format(it) }
}