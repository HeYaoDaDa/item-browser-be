package `fun`.hydd.cdda.item_browser_be.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToOne
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class GettextPo(
  @NotNull @NotBlank var languageCode: String,
  @NotNull @ManyToOne @JoinColumn var version: Version,
  @NotNull @OneToOne @JoinColumn var hashJson: HashJson,
  @Id @GeneratedValue var id: Long,
)