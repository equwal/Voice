package voice.features.playbackScreen

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import voice.core.data.BookId
import voice.core.data.repo.SubtitleRepo

class FakeSubtitleRepo(initial: Map<BookId, Uri> = emptyMap()) : SubtitleRepo {

  private val uris = MutableStateFlow(initial)

  override fun uriFlow(bookId: BookId): Flow<Uri?> = uris.map { it[bookId] }

  override suspend fun setUri(
    bookId: BookId,
    uri: Uri,
  ) {
    uris.value = uris.value + (bookId to uri)
  }

  override suspend fun removeUri(bookId: BookId) {
    uris.value = uris.value - bookId
  }
}
