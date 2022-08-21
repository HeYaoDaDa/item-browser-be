package `fun`.hydd.cdda.item_browser_be.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class CddaJson(
    @NotNull @NotBlank var type: String,
    @NotNull @NotBlank var modId: String,
    @NotNull @NotBlank var path: String,
    @OneToOne @JoinColumn var json: HashJson,
    @Id @GeneratedValue var id: Long,
)