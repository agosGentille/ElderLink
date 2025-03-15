package tpi.tusi.ui.adapters

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import tpi.tusi.ui.entities.Etiquetas

class EtiquetaAdapter(context: Context, etiquetas: List<Etiquetas>) :
    ArrayAdapter<Etiquetas>(context, android.R.layout.simple_spinner_item, etiquetas) {

    init {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent) as TextView
        view.text = getItem(position)?.nombre
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent) as TextView
        view.text = getItem(position)?.nombre
        return view
    }
}
