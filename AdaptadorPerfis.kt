package com.seu.meumouse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdaptadorPerfis(
    val lista: MutableList<PerfilJogo>,
    val aoClicar: (PerfilJogo) -> Unit,
    val aoExcluir: (PerfilJogo) -> Unit
) : RecyclerView.Adapter<AdaptadorPerfis.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icone: TextView = itemView.findViewById(R.id.iconePerfil)
        val nome: TextView = itemView.findViewById(R.id.nomePerfil)
        val pacote: TextView = itemView.findViewById(R.id.pacotePerfil)
        val botaoExcluir: ImageButton = itemView.findViewById(R.id.botaoExcluirPerfil)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_perfil, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, posicao: Int) {
        val perfil = lista[posicao]
        
        holder.icone.text = perfil.icone
        holder.nome.text = perfil.nome
        holder.pacote.text = perfil.pacote
        
        holder.itemView.setOnClickListener {
            aoClicar(perfil)
        }
        
        holder.botaoExcluir.setOnClickListener {
            aoExcluir(perfil)
        }
    }

    override fun getItemCount(): Int = lista.size
}
