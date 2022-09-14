package `fun`.hydd.cdda.item_browser_be.entity

import javax.persistence.ElementCollection
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.OrderColumn
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class CddaItem(
  @NotNull @NotBlank var type: String?,
  @NotNull @NotBlank var itemId: String?,
  @NotNull @NotBlank var name: String?,
  var describe: String?,
  @ElementCollection @OrderColumn var premodIds: List<String>?,
  @NotNull @NotBlank var jsonType: String,
  @NotNull @NotBlank var modId: String,
  @NotNull @NotBlank var path: String,
  @OneToOne @JoinColumn var oldJson: HashJson,
  @Embedded var copyFromRef: CddaItemRef?,
  @NotNull @OneToOne @JoinColumn var json: HashJson?,
  @Id @GeneratedValue var id: Long?,
)