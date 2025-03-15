package tpi.tusi.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button
import androidx.fragment.app.Fragment;
import tpi.tusi.R
import tpi.tusi.ui.activities.AccesoActivity
import tpi.tusi.ui.activities.AgregarAutoevaluacionActivity
import tpi.tusi.ui.activities.AgregarModificarEventoActivity
import tpi.tusi.ui.activities.AgregarPreguntaAutoevaluacionActivity
import tpi.tusi.ui.activities.GestionEstadoCursoActivity
import tpi.tusi.ui.activities.GestionEstadoGestorActivity
import tpi.tusi.ui.activities.ListadoEventosActivity
import tpi.tusi.ui.activities.MainAdminActivity
import tpi.tusi.ui.activities.MainEstudianteActivity
import tpi.tusi.ui.activities.MainGestorActivity
import tpi.tusi.ui.activities.NotaFinalAutoevaluacionActivity
import tpi.tusi.ui.activities.RealizarAutoevaluacionAlumnoActivity
import tpi.tusi.ui.activities.ReporteActivity
import tpi.tusi.ui.activities.VerEventoActivity

class HomeFragment : Fragment() {

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        /*Botones de activities*/
        val btnA = view.findViewById<Button>(R.id.AccesoActivity)
        val btnAAEA = view.findViewById<Button>(R.id.btn_AgregarAutoEvaluacionActivity)
        val btnAMEA = view.findViewById<Button>(R.id.btn_AgregarModificarEventoActivity)
        val btnEventoModificar = view.findViewById<Button>(R.id.btn_ModificarEventoActivity)
        val btnAPAEA = view.findViewById<Button>(R.id.btn_AgregarPreguntaAutoEvalulacionActivity)
        val btnLEA = view.findViewById<Button>(R.id.btn_ListadoEventosActivity)
        val btnMA = view.findViewById<Button>(R.id.btn_MainAdminActivity)
        val btnME = view.findViewById<Button>(R.id.btn_MainEstudianteActivity)
        val btnMG = view.findViewById<Button>(R.id.btn_MainGestorActivity)
        val btnNFAEA = view.findViewById<Button>(R.id.btn_NotaFinalAutoEvaluacionActivity)
        val btnRAEAA = view.findViewById<Button>(R.id.btn_RealizarAutoEvaluacionAlumnoActivity)
        val btnVEA = view.findViewById<Button>(R.id.btn_VerEventoActivity)
        val btnCurso = view.findViewById<Button>(R.id.btn_verCursoEspecificoActivity)
        val btnReporte = view.findViewById<Button>(R.id.btn_Reportes)

        /*Botones de fragment*/
        val btnCCF = view.findViewById<Button>(R.id.btn_CrearCurso)
        val btnLCF = view.findViewById<Button>(R.id.ListarCursos)
        val btnLGF = view.findViewById<Button>(R.id.btn_ListarGestores)
        val btnLF = view.findViewById<Button>(R.id.btn_Login)
        val btnMCF = view.findViewById<Button>(R.id.btn_MiCuenta)
        val btnR1F = view.findViewById<Button>(R.id.btn_Registro)
        val btnR2F = view.findViewById<Button>(R.id.btn_Registro2)
        val btnGestionEstadoCurso= view.findViewById<Button>(R.id.btn_EstadoCurso)
        val btnGestionEstadoGestor= view.findViewById<Button>(R.id.btn_EstadoGestor)

        btnReporte.setOnClickListener {
            val intent = Intent(requireActivity(), ReporteActivity::class.java)
            startActivity(intent)
        }

        btnEventoModificar.setOnClickListener {
            val intent = Intent(requireActivity(), AgregarModificarEventoActivity::class.java)
            intent.putExtra("evento", 1L) //mando 1L para modificar el evento con id 1
            startActivity(intent)
        }

        btnA.setOnClickListener {
            val intent = Intent(requireActivity(), AccesoActivity::class.java)
            startActivity(intent)
        }
        btnAAEA.setOnClickListener{
            val intent = Intent(requireActivity(), AgregarAutoevaluacionActivity::class.java)
            startActivity(intent)
        }
        btnAMEA.setOnClickListener{
            val intent = Intent(requireActivity(), AgregarModificarEventoActivity::class.java)
            startActivity(intent)
        }
        btnAPAEA.setOnClickListener{
            val intent = Intent(requireActivity(), AgregarPreguntaAutoevaluacionActivity::class.java)
            startActivity(intent)
        }
        btnLEA.setOnClickListener{
            val intent = Intent(requireActivity(), ListadoEventosActivity::class.java)
            startActivity(intent)
        }
        btnMA.setOnClickListener{
            val intent = Intent(requireActivity(), MainAdminActivity::class.java)
            startActivity(intent)
        }
        btnME.setOnClickListener{
            val intent = Intent(requireActivity(), MainEstudianteActivity::class.java)
            startActivity(intent)
        }
        btnMG.setOnClickListener{
            val intent = Intent(requireActivity(), MainGestorActivity::class.java)
            startActivity(intent)
        }
        btnNFAEA.setOnClickListener{
            val intent = Intent(requireActivity(), NotaFinalAutoevaluacionActivity::class.java)
            startActivity(intent)
        }
        btnRAEAA.setOnClickListener{
            val intent = Intent(requireActivity(), RealizarAutoevaluacionAlumnoActivity::class.java)
            startActivity(intent)
        }
        btnVEA.setOnClickListener{
            val intent = Intent(requireActivity(), VerEventoActivity::class.java)
            startActivity(intent)
        }

        btnCCF.setOnClickListener {
            val fragment = CrearCursoFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        btnGestionEstadoCurso.setOnClickListener {
            val intent = Intent(requireActivity(), GestionEstadoCursoActivity::class.java)
            startActivity(intent)
        }
        btnGestionEstadoGestor.setOnClickListener {
            val intent = Intent(requireActivity(), GestionEstadoGestorActivity::class.java)
            startActivity(intent)
        }
        btnLCF.setOnClickListener {
            val fragment = ListarCursosAdminFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        btnLGF.setOnClickListener {
            val fragment = ListarGestoresFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        btnLF.setOnClickListener {
            val fragment = LoginFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        btnMCF.setOnClickListener {
            val fragment = MiCuentaFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        btnR1F.setOnClickListener {
            val fragment = RegistroPt1Fragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        btnR2F.setOnClickListener {
            val fragment = RegistroPt2Fragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    return view;
    }
}

