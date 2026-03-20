package nl.jacobras.codebaseobserver.modulegraph

internal enum class ModuleColors(val hex: String) {
    Green("#caffbf"),
    Orange("#ffd6a5"),
    Blue("#9bf6ff"),
    Purple("#bdb2ff"),
    Red("#ffadad");

    companion object {
        fun fromHex(hex: String): ModuleColors? {
            return entries.find { it.hex == hex }
        }
    }
}