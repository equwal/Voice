package voice.features.playbackScreen.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shows the subtitle cue that is on at the current playback position.
 *
 * Cues can run to a few hundred characters, so the text wraps and the row scrolls within a fixed
 * height instead of clipping or pushing the rest of the screen around.
 */
@Composable
internal fun SubtitleRow(
  text: String?,
  modifier: Modifier = Modifier,
  maxHeight: Dp = 140.dp,
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .heightIn(max = maxHeight)
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 16.dp, vertical = 4.dp),
    contentAlignment = Alignment.Center,
  ) {
    SelectionContainer {
      Text(
        text = text.orEmpty(),
        style = MaterialTheme.typography.titleLarge,
        textAlign = TextAlign.Center,
      )
    }
  }
}
