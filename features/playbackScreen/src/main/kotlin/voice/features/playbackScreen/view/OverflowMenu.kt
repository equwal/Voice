package voice.features.playbackScreen.view

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import voice.core.data.toUri
import voice.core.strings.R
import voice.core.ui.icons.VoiceIcons
import voice.features.playbackScreen.BookPlayViewState
import voice.features.playbackScreen.subtitles.SubReadContract

private val EBOOK_MIME_TYPES = arrayOf(
  "application/epub+zip",
  "text/plain",
  "application/zip",
  "application/octet-stream",
)

@Composable
internal fun OverflowMenu(
  skipSilence: Boolean,
  subtitlesEnabled: Boolean,
  subtitleGeneration: BookPlayViewState.SubtitleGenerationViewState,
  onSkipSilenceClick: () -> Unit,
  onVolumeBoostClick: () -> Unit,
  onSubtitleFileSelect: (Uri) -> Unit,
  onRemoveSubtitlesClick: () -> Unit,
  onSubtitlesGenerationResult: (srtUri: Uri?, cues: Int, matchRate: Double, error: String?) -> Unit,
) {
  Box {
    var expanded by remember { mutableStateOf(false) }
    var showSubReadNotInstalled by remember { mutableStateOf(false) }
    var pendingAudioUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val subtitlePickerLauncher = rememberLauncherForActivityResult(
      ActivityResultContracts.OpenDocument(),
    ) { uri ->
      if (uri != null) {
        onSubtitleFileSelect(uri)
      }
    }
    val subReadLauncher = rememberLauncherForActivityResult(
      ActivityResultContracts.StartActivityForResult(),
    ) { result ->
      val data = result.data
      onSubtitlesGenerationResult(
        data?.data,
        data?.getIntExtra(SubReadContract.EXTRA_CUES, 0) ?: 0,
        data?.getDoubleExtra(SubReadContract.EXTRA_MATCH_RATE, 1.0) ?: 1.0,
        data?.getStringExtra(SubReadContract.EXTRA_ERROR),
      )
    }
    val ebookPickerLauncher = rememberLauncherForActivityResult(
      ActivityResultContracts.OpenDocument(),
    ) { ebookUri ->
      val audioUri = pendingAudioUri
      if (ebookUri != null && audioUri != null) {
        subReadLauncher.launch(SubReadContract.alignIntent(audioUri, ebookUri))
      }
      pendingAudioUri = null
    }

    IconButton(
      onClick = {
        expanded = !expanded
      },
    ) {
      Icon(
        imageVector = VoiceIcons.MoreVert,
        contentDescription = stringResource(id = R.string.common_action_more),
      )
    }
    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
    ) {
      DropdownMenuItem(
        onClick = {
          expanded = false
          onSkipSilenceClick()
        },
        text = {
          Text(text = stringResource(id = R.string.playback_option_skip_silence))
        },
        trailingIcon = {
          Checkbox(
            checked = skipSilence,
            onCheckedChange = {
              expanded = false
              onSkipSilenceClick()
            },
          )
        },
      )
      DropdownMenuItem(
        onClick = {
          expanded = false
          onVolumeBoostClick()
        },
        text = {
          Text(text = stringResource(id = R.string.playback_option_volume_boost))
        },
      )
      DropdownMenuItem(
        onClick = {
          expanded = false
          subtitlePickerLauncher.launch(arrayOf("*/*"))
        },
        text = {
          Text(text = stringResource(id = R.string.playback_option_subtitles))
        },
      )
      if (subtitlesEnabled) {
        DropdownMenuItem(
          onClick = {
            expanded = false
            onRemoveSubtitlesClick()
          },
          text = {
            Text(text = stringResource(id = R.string.playback_option_subtitles_remove))
          },
        )
      }
      val audioUri = (subtitleGeneration as? BookPlayViewState.SubtitleGenerationViewState.Available)?.chapterId?.toUri()
      DropdownMenuItem(
        enabled = audioUri != null,
        onClick = {
          expanded = false
          if (audioUri != null) {
            if (SubReadContract.isInstalled(context)) {
              pendingAudioUri = audioUri
              ebookPickerLauncher.launch(EBOOK_MIME_TYPES)
            } else {
              showSubReadNotInstalled = true
            }
          }
        },
        text = {
          Text(text = stringResource(id = R.string.playback_option_subread))
        },
      )
    }
    if (showSubReadNotInstalled) {
      SubReadNotInstalledDialog(onDismiss = { showSubReadNotInstalled = false })
    }
  }
}

@Composable
private fun SubReadNotInstalledDialog(onDismiss: () -> Unit) {
  val uriHandler = LocalUriHandler.current
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(text = stringResource(id = R.string.playback_subread_not_installed_title))
    },
    text = {
      Text(text = stringResource(id = R.string.playback_subread_not_installed_message))
    },
    confirmButton = {
      TextButton(
        onClick = {
          uriHandler.openUri(SubReadContract.RELEASES_URL)
          onDismiss()
        },
      ) {
        Text(text = stringResource(id = R.string.playback_subread_not_installed_action))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(text = stringResource(id = R.string.common_dialog_cancel))
      }
    },
  )
}
