package project.inflabnet.mytest.mesas.adapter

import project.inflabnet.mytest.R
import project.inflabnet.mytest.mesas.model.Mesa
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.mesa_item.view.*
import project.inflabnet.mytest.mesas.activity.MesaActivity


class MesaAdapter(
	val mesa: ArrayList<Mesa>,
	val itemClick: (Mesa) -> Unit
) :
    RecyclerView.Adapter<MesaAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mesa_item, parent, false)
        return ViewHolder(view, itemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindForecast(mesa[position])
    }

    override fun getItemCount() = mesa.size

    class ViewHolder(view: View, val itemClick: (Mesa) -> Unit) : RecyclerView.ViewHolder(view) {

        fun bindForecast(mesa: Mesa) {
            with(mesa) {
                itemView.txtMesa.text = mesa.nameMesa
                itemView.txtProp.text = mesa.proprietarioMesa
                itemView.txtTS.text = mesa.timestamp
                //itemView.setOnClickListener { itemClick(this) }
                itemView.btnDividirConta.setOnClickListener { itemClick(this) }
            }
        }
    }
}