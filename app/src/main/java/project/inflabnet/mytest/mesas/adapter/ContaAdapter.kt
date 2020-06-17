package project.inflabnet.mytest.mesas.adapter

import project.inflabnet.mytest.R
import project.inflabnet.mytest.mesas.model.Conta
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_consumido.view.*

class ContaAdapter(val conta: ArrayList<Conta>, private val itemClick: (Conta) -> Unit) :
    RecyclerView.Adapter<ContaAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_consumido, parent, false)
        return ViewHolder(view, itemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindForecast(conta[position])
    }

    override fun getItemCount() = conta.size

    class ViewHolder(view: View, val itemClick: (Conta) -> Unit) : RecyclerView.ViewHolder(view) {

        fun bindForecast(conta: Conta) {
            with(conta) {
                itemView.txtQuem.text = conta.quem
                itemView.txtQuanto.text = conta.quanto.toString()
                itemView.txtOq.text = conta.oque
                itemView.txtTimestamp.text = conta.timestamp.toString()
                itemView.setOnClickListener { itemClick(this) }
            }
        }
    }
}