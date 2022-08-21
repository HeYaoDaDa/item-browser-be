package `fun`.hydd.cdda.item_browser_be.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class GithubRelease(
  @JsonProperty("name") val name: String,
  @JsonProperty("tag_name") val tagName: String,
  @JsonProperty("target_commitish") val commitHash: String,
  @JsonProperty("prerelease") val isExperiment: Boolean,
  @JsonProperty("published_at") val publishDate: Date
)
