package voice.features.playbackScreen.subtitles

import android.app.Application
import android.net.Uri
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import voice.core.data.BookId
import java.io.File
import java.security.MessageDigest

/**
 * Copies a subtitle file into app-private storage, so Voice keeps a subtitle Voice generated
 * itself even after the source content uri's short-lived grant ends.
 */
@Inject
class SubtitleFileStore(private val context: Application) {

  /** Reads [source] fully and writes it under `filesDir/subtitles`, named after [bookId]. Returns the new `file` uri. */
  suspend fun copyToAppStorage(
    bookId: BookId,
    source: Uri,
  ): Uri = withContext(Dispatchers.IO) {
    val directory = File(context.filesDir, "subtitles").apply { mkdirs() }
    val file = File(directory, "${bookId.hash()}.srt")
    val input = context.contentResolver.openInputStream(source)
      ?: error("Could not open $source")
    input.use { stream ->
      file.outputStream().use { output ->
        stream.copyTo(output)
      }
    }
    Uri.fromFile(file)
  }

  private fun BookId.hash(): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
    return digest.joinToString("") { "%02x".format(it) }
  }
}
