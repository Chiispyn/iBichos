package com.cetecom.ibichos.domain.model.enums

/**
 * Registro centralizado de todas las medallas (logros) disponibles en el sistema.
 * Mapea el ID guardado en la base de datos a sus recursos visuales en español.
 */
enum class MedalInfo(
    val id: String,
    val title: String,
    val description: String,
    val icon: String
) {
    FIRST_CAPTURE(
        id = "FIRST_CAPTURE",
        title = "Primer Avistamiento",
        description = "Has descubierto tu primera especie única. ¡El inicio de tu viaje!",
        icon = "🥉"
    ),
    NOVICE_RESEARCHER(
        id = "NOVICE_RESEARCHER",
        title = "Investigador Novato",
        description = "Has descubierto 5 especies únicas diferentes.",
        icon = "🥈"
    ),
    BRAVE_HUNTER(
        id = "BRAVE_HUNTER",
        title = "Cazador Valiente",
        description = "Has registrado una especie catalogada como venenosa o de precaución.",
        icon = "💀"
    ),
    ARACHNOLOGIST(
        id = "ARACHNOLOGIST",
        title = "Aracnólogo",
        description = "Has capturado al menos 10 especies diferentes de arácnidos.",
        icon = "🕷️"
    ),
    LEPIDOPTEROLOGIST(
        id = "LEPIDOPTEROLOGIST",
        title = "Lepidopterólogo",
        description = "Has capturado al menos 10 especies diferentes de mariposas o polillas.",
        icon = "🦋"
    ),
    POLLINATOR_FRIEND(
        id = "POLLINATOR_FRIEND",
        title = "Amigo Polinizador",
        description = "Has capturado al menos 10 especies diferentes de abejas, avispas u hormigas.",
        icon = "🐝"
    ),
    COLEOPTEROLOGIST(
        id = "COLEOPTEROLOGIST",
        title = "Coleopterólogo",
        description = "Has capturado al menos 15 especies diferentes de escarabajos.",
        icon = "🪲"
    );

    companion object {
        fun fromId(id: String): MedalInfo? {
            return values().find { it.id == id }
        }
    }
}
