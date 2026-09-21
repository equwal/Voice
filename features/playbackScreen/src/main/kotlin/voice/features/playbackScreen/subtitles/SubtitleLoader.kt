package voice.features.playbackScreen.subtitles

import android.app.Application
import android.net.Uri
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import voice.core.logging.api.Logger
import voice.core.subtitles.SrtParser
import voice.core.subtitles.SubtitleCueIndex

/**
 * Reads an SRT file behind a content [Uri] and builds a lookup index from it.
 *
 * Audiobook SRT files can carry thousands of cues, so parsing runs off the main thread.
 */
@Inject
class SubtitleLoader(private val context: Application) {

  suspend fun load(uri: Uri): SubtitleCueIndex? = withContext(Dispatchers.IO) {
    try {
      val content = context.contentResolver.openInputStream(uri)?.use { input ->
        input.readBytes().toString(Charsets.UTF_8)
      } ?: return@withContext null
      SubtitleCueIndex(SrtParser.parse(content))
    } catch (e: Exception) {
      Logger.w(e, "Could not load subtitles from $uri")
      null
    }
  }
}
