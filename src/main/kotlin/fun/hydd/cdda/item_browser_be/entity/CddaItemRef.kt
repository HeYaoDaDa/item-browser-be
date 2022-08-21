package `fun`.hydd.cdda.item_browser_be.entity

import javax.persistence.Embeddable
import javax.validation.constraints.NotNull

@Embeddable
data class CddaItemRef(
    @NotNull var type: String,
    @NotNull var id: String,
)