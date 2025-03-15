package tpi.tusi.ui.entities

data class Provincias(
    val id_provincia: Long,
    val nombre: String,
    val pais: Long
){
    override fun toString(): String {
        return nombre;
    }
}
