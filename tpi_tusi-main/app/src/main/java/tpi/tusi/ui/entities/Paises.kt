package tpi.tusi.ui.entities

data class Paises(
    val id_pais: Long,
    val nombre: String,
){
    //Para que en los spinner que muestren los países muestren solo el nombre
    override fun toString(): String {
        return nombre;
    }
}
