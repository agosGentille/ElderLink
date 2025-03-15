package tpi.tusi.ui.entities

data class Ciudades(
    val id_ciudad: Long,
    val nombre: String,
    val provincia: Long,
){
    override fun toString(): String {
        return nombre;
    }
}
