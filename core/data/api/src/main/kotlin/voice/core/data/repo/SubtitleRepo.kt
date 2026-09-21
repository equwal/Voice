package voice.core.data.repo

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import voice.core.data.BookId

/**
 * Persists which SRT subtitle file, if any, is loaded for a book.
 */
public interface SubtitleRepo {

  /** The subtitle file [Uri] for [bookId], or `null` if none is set. */
  public fun uriFlow(bookId: BookId): Flow<Uri?>

  /** Sets the subtitle file for [bookId], replacing any previous one. */
  public suspend fun setUri(
    bookId: BookId,
    uri: Uri,
  )

  /** Removes the subtitle file for [bookId], if any. */
  public suspend fun removeUri(bookId: BookId)
}
