package project.inflabnet.mytest.mesas.activity

import project.inflabnet.mytest.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.membros_mesa_local.view.*

class MembrosAdapter(val toMembros: MutableList<MembroLocal>, private val itemClick: (MembroLocal) -> Unit) : RecyclerView.Adapter<MembrosAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.membros_mesa_local,parent,false)
        return ViewHolder(view,itemClick)
    }

    override fun getItemCount(): Int {
        return toMembros.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindForecast(toMembros[position])
    }

    class ViewHolder(view: View, val itemClick: (MembroLocal) -> Unit) : RecyclerView.ViewHolder(view) {
        fun bindForecast(membro: MembroLocal) {
            with(membro) {
                itemView.txtPessoa.text = membro.user
                if (!membro.location.equals("Sem Local")){
                    itemView.btnLocali.visibility = View.VISIBLE
                    itemView.btnLocali.setOnClickListener { itemClick(this) }
                }

            }
        }
    }
}
