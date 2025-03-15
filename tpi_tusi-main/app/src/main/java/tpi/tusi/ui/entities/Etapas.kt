package tpi.tusi.ui.entities

import android.os.Parcel
import android.os.Parcelable

data class Etapas(
    val id_etapa: Long,
    var titulo: String,
    var contenido: String,
    val activo: Boolean,
    val fk_curso: Long
): Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id_etapa)
        parcel.writeString(titulo)
        parcel.writeString(contenido)
        parcel.writeByte(if (activo) 1 else 0)
        parcel.writeLong(fk_curso)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Etapas> {
        override fun createFromParcel(parcel: Parcel): Etapas {
            return Etapas(parcel)
        }

        override fun newArray(size: Int): Array<Etapas?> {
            return arrayOfNulls(size)
        }
    }
}
