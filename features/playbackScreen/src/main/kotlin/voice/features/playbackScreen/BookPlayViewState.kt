package voice.features.playbackScreen

import androidx.compose.runtime.Immutable
import voice.core.data.ChapterId
import voice.core.playback.misc.Decibel
import voice.features.sleepTimer.SleepTimerViewState
import kotlin.time.Duration

@Immutable
data class BookPlayViewState(
  val chapterName: String?,
  val showPreviousNextButtons: Boolean,
  val title: String,
  val sleepTimerState: SleepTimerViewState,
  val playedTime: Duration,
  val duration: Duration,
  val playing: Boolean,
  val cover: String?,
  val skipSilence: Boolean,
  val subtitle: SubtitleViewState,
  val subtitleGeneration: SubtitleGenerationViewState,
) {

  sealed interface SleepTimerViewState {
    data object Disabled : SleepTimerViewState

    sealed interface Enabled : SleepTimerViewState {
      data object WithEndOfChapter : Enabled

      @JvmInline
      value class WithDuration(val leftDuration: Duration) : Enabled
    }
  }

  sealed interface SubtitleViewState {
    data object Disabled : SubtitleViewState

    /** [text] is `null` when no cue is on at the current position. */
    data class Enabled(val text: String?) : SubtitleViewState
  }

  /** Whether the "Make subtitles with SubRead" menu item can be offered for this book. */
  sealed interface SubtitleGenerationViewState {
    /** SubRead handles one audio file per book, and this book has more than one. */
    data object Unavailable : SubtitleGenerationViewState

    /** This book has exactly one audio file, whose file uri is derived from [chapterId]. */
    data class Available(val chapterId: ChapterId) : SubtitleGenerationViewState
  }

  init {
    require(duration > Duration.ZERO) {
      "Duration must be positive in $this"
    }
  }
}

internal sealed interface BookPlayDialogViewState {
  data class SpeedDialog(val speed: Float) : BookPlayDialogViewState {

    val maxSpeed: Float get() = if (speed < 2F) 2F else 3.5F
  }

  data class VolumeGainDialog(
    val gain: Decibel,
    val valueFormatted: String,
    val maxGain: Decibel,
  ) : BookPlayDialogViewState

  data class SelectChapterDialog(val items: List<ItemViewState>) : BookPlayDialogViewState {

    data class ItemViewState(
      val number: Int,
      val name: String,
      val active: Boolean,
      val time: String,
    )
  }

  @JvmInline
  value class SleepTimer(val viewState: SleepTimerViewState) : BookPlayDialogViewState
}
