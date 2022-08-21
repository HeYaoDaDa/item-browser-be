package `fun`.hydd.cdda.item_browser_be.entity

import io.vertx.core.json.JsonObject
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.validation.constraints.Max
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class HashJson(
    @NotNull @NotBlank @Max(64) var hash: String,
    @NotNull @NotBlank var json: JsonObject,
    @Id @GeneratedValue var id: Long,
)