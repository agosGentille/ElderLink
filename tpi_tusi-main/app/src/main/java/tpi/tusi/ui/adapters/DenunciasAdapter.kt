package tpi.tusi.ui.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tpi.tusi.R
import tpi.tusi.data.daos.DenunciasDao
import tpi.tusi.ui.activities.GestionEstadoCursoActivity
import tpi.tusi.ui.activities.vistaEspecificaCurso
import tpi.tusi.ui.entities.Cursos
import tpi.tusi.ui.entities.DenunciaDetalle
import tpi.tusi.ui.entities.Denuncias
import tpi.tusi.ui.entities.Usuarios

class DenunciasAdapter(
    private val context: Context,
    private val cursosDenunciados: MutableList<Triple<Long, String, List<DenunciaDetalle>>>
                                    //Triple (idCurso, tituloCurso, detallesDenuncia)
) : BaseExpandableListAdapter() {

    override fun getGroupCount(): Int {
        return cursosDenunciados.size
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return cursosDenunciados[groupPosition].third.size
    }

    override fun getGroup(groupPosition: Int): Any {
        return cursosDenunciados[groupPosition].second
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        return cursosDenunciados[groupPosition].third[childPosition]
    }

    override fun getGroupId(groupPosition: Int): Long {
        return cursosDenunciados[groupPosition].first
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return cursosDenunciados[groupPosition].third[childPosition].denuncia_id
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_expandable_list_denuncias, parent, false)

        val tituloCurso = cursosDenunciados[groupPosition].second
        val idCurso = cursosDenunciados[groupPosition].first
        val groupTextView = view.findViewById<TextView>(R.id.groupName)
        groupTextView.text = tituloCurso

        val container = view.findViewById<LinearLayout>(R.id.container)
        val btnVerCurso = view.findViewById<ImageButton>(R.id.btnVerCurso)
        val btnGestionarEstadoCurso = view.findViewById<ImageButton>(R.id.btnGestionarEstadoCurso)

        btnVerCurso.setOnClickListener {
            val intent = Intent(context, vistaEspecificaCurso::class.java).apply {
                putExtra("curso_Id", idCurso)
            }
            context.startActivity(intent)
        }

        btnGestionarEstadoCurso.setOnClickListener{
            val intent = Intent(context, GestionEstadoCursoActivity::class.java).apply {
                putExtra("id", idCurso)
            }
            context.startActivity(intent)
        }

        container.setOnClickListener {
            val expandableListView = parent as? ExpandableListView
            if (isExpanded) {
                expandableListView?.collapseGroup(groupPosition)
            } else {
                expandableListView?.expandGroup(groupPosition)
            }
        }

        return view
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.sub_item_expandable_list_denuncias, parent, false)

        val denunciaDetalle = getChild(groupPosition, childPosition) as DenunciaDetalle
        val userTextView = view.findViewById<TextView>(R.id.usuarioDenuncia)
        val razonTextView = view.findViewById<TextView>(R.id.razonDenuncia)
        val btnEliminarDenuncia = view.findViewById<ImageButton>(R.id.btnEliminarDenuncia)

        userTextView.text = denunciaDetalle.fk_usuario_mail
        razonTextView.text = denunciaDetalle.denuncia_razon
        btnEliminarDenuncia.setOnClickListener{
            AlertDialog.Builder(context)
                .setTitle("Confirmación de Eliminación")
                .setMessage("¿Estás seguro de que deseas eliminar esta denuncia?")
                .setPositiveButton("Sí") { dialog, _ ->
                    val denunciaList = cursosDenunciados[groupPosition].third.toMutableList()
                    denunciaList.removeAt(childPosition)
                    if (denunciaList.isEmpty()) {
                        cursosDenunciados.removeAt(groupPosition)
                    } else {
                        cursosDenunciados[groupPosition] = Triple(
                            cursosDenunciados[groupPosition].first,
                            cursosDenunciados[groupPosition].second,
                            denunciaList
                        )
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        val denunciaId = denunciaDetalle.denuncia_id
                        val dDao = DenunciasDao()
                        dDao.deleteDenuncia(denunciaId)
                    }
                    notifyDataSetChanged()
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

        return view
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}