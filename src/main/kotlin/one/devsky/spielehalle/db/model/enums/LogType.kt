package one.devsky.spielehalle.db.model.enums

import java.awt.Color

enum class LogType(val color: Color) {
    INFO(Color(0x69BA69)),
    WARNING(Color(0xF1F193)),
    ERROR(Color(0xCE7A7A)),
    DEBUG(Color(0x5E5EFF)),
    CUSTOM(Color(0x000000))
}