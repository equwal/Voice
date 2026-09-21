package voice.features.playbackScreen.subtitles

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * The intent contract for the SubRead app (`space.subread.app`), which makes an SRT subtitle
 * file for a book from its audio and its ebook.
 *
 * See https://subread.space.
 */
internal object SubReadContract {

  private const val PACKAGE = "space.subread.app"
  private const val ACTION_ALIGN = "space.subread.app.action.ALIGN"
  private const val EXTRA_AUDIO = "space.subread.extra.AUDIO"
  private const val EXTRA_BOOK = "space.subread.extra.BOOK"

  const val EXTRA_CUES: String = "space.subread.extra.CUES"
  const val EXTRA_MATCH_RATE: String = "space.subread.extra.MATCH_RATE"
  const val EXTRA_ERROR: String = "space.subread.extra.ERROR"

  /** Below this match rate, the ebook likely does not match the audio, for example another edition. */
  const val LOW_MATCH_RATE_THRESHOLD: Double = 0.8

  const val RELEASES_URL: String = "https://github.com/equwal/subread-android/releases/latest"

  /** Builds the request to align [audioUri] against [bookUri] and grants SubRead read access to both. */
  fun alignIntent(
    audioUri: Uri,
    bookUri: Uri,
  ): Intent {
    return Intent(ACTION_ALIGN).apply {
      setPackage(PACKAGE)
      putExtra(EXTRA_AUDIO, audioUri)
      putExtra(EXTRA_BOOK, bookUri)
      clipData = ClipData.newRawUri("audio", audioUri).apply {
        addItem(ClipData.Item(bookUri))
      }
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
  }

  /** `true` if an app on the device can resolve [ACTION_ALIGN] for SubRead's package. */
  fun isInstalled(context: Context): Boolean {
    val probe = Intent(ACTION_ALIGN).setPackage(PACKAGE)
    return probe.resolveActivity(context.packageManager) != null
  }
}
