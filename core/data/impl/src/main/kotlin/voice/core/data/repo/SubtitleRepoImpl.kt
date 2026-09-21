package voice.core.data.repo

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
    try {
      context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    } catch (e: SecurityException) {
      Logger.w(e, "Could not take persistable uri permission for $uri")
    }
    store.updateData { it + (bookId to uri) }
  }

  override suspend fun removeUri(bookId: BookId) {
    val previous = store.data.first()[bookId]
    store.updateData { it - bookId }
    if (previous != null) {
      try {
        context.contentResolver.releasePersistableUriPermission(previous, Intent.FLAG_GRANT_READ_URI_PERMISSION)
      } catch (e: SecurityException) {
        Logger.w(e, "Could not release persistable uri permission for $previous")
      }
    }
  }
}
