package voice.features.playbackScreen

internal sealed interface BookPlayViewEffect {
  data object BookmarkAdded : BookPlayViewEffect
  data object RequestIgnoreBatteryOptimization : BookPlayViewEffect

  /** SubRead finished. [cues] is the number of subtitle lines it created, [matchRate] is 0 to 1. */
  data class SubtitleGenerationSucceeded(
    val cues: Int,
    val matchRate: Double,
  ) : BookPlayViewEffect

  /** SubRead reported [message] as the reason it could not create subtitles. */
  data class SubtitleGenerationFailed(val message: String) : BookPlayViewEffect
}
