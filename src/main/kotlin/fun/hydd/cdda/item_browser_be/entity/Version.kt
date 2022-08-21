package `fun`.hydd.cdda.item_browser_be.entity

import `fun`.hydd.cdda.item_browser_be.constant.VersionStatus
import java.time.OffsetDateTime
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.OneToMany
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class Version(
  @NotNull var status: VersionStatus,
  @NotNull @NotBlank var tagName: String,
  @NotNull @NotBlank var releaseName: String,
  @NotNull var isExperiment: Boolean,
  @NotNull @NotBlank var tagHash: String,
  @NotNull var publishDate: OffsetDateTime,
  @NotNull var tagDate: OffsetDateTime,
  @ManyToMany @JoinTable var cddaItems: List<CddaItem>,
  @OneToMany(mappedBy = "version") var gettextPo: List<GettextPo>,
  @Id @GeneratedValue var id: Long?,
)