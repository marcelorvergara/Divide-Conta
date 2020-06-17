package project.inflabnet.mytest.database.adapter

import project.inflabnet.mytest.R
import project.inflabnet.mytest.database.model.MesaOrc
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.orcamento_item.view.*

class GastosAdapter(val gastos: List<MesaOrc>, private val itemClick: (MesaOrc) -> Unit)
    :RecyclerView.Adapter<project.inflabnet.mytest.database.adapter.GastosAdapter.ViewHolder> (){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GastosAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.orcamento_item,parent,false)
        return GastosAdapter.ViewHolder(
			view,
			itemClick
		)
    }

    override fun getItemCount(): Int {
        return gastos.size
    }

    override fun onBindViewHolder(holder: GastosAdapter.ViewHolder, position: Int) {
        holder.bindForecast(gastos[position])
    }

    class ViewHolder(view: View, val itemClick: (MesaOrc) -> Unit):RecyclerView.ViewHolder(view){
        fun bindForecast(gastos: MesaOrc) {
            with(gastos) {
                itemView.txtMonth.text = gastos.mesMesa
                itemView.txtTable.text = gastos.nome_mesa
                itemView.txtGastos.text = gastos.gasto.toString()
                itemView.setOnClickListener { itemClick(this) }
            }
        }
    }
}