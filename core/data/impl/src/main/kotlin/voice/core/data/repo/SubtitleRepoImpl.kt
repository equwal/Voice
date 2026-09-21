package voice.core.data.repo

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.datastore.core.DataStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import voice.core.data.BookId
import voice.core.data.store.SubtitleUriStore
import voice.core.logging.api.Logger
import java.io.File

@ContributesBinding(AppScope::class)
public class SubtitleRepoImpl
internal constructor(
  @SubtitleUriStore
  private val store: DataStore<Map<BookId, Uri>>,
  private val context: Context,
) : SubtitleRepo {

  override fun uriFlow(bookId: BookId): Flow<Uri?> {
    return store.data.map { it[bookId] }
  }

  override suspend fun setUri(
    bookId: BookId,
    uri: Uri,
  ) {
    // A `content` uri comes from a document picker and needs a persisted grant to survive a
    // reboot. A `file` uri points at our own app-private storage, so we already own it.
    if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
      try {
        context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
      } catch (e: SecurityException) {
        Logger.w(e, "Could not take persistable uri permission for $uri")
      }
    }
    store.updateData { it + (bookId to uri) }
  }

  override suspend fun removeUri(bookId: BookId) {
    val previous = store.data.first()[bookId]
    store.updateData { it - bookId }
    if (previous != null) {
      if (previous.scheme == ContentResolver.SCHEME_CONTENT) {
        try {
          context.contentResolver.releasePersistableUriPermission(previous, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: SecurityException) {
          Logger.w(e, "Could not release persistable uri permission for $previous")
        }
      } else if (previous.scheme == ContentResolver.SCHEME_FILE) {
        previous.path?.let { File(it).delete() }
      }
    }
  }
}
