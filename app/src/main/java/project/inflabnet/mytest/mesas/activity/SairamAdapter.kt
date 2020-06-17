package project.inflabnet.mytest.mesas.activity

import project.inflabnet.mytest.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.sairam_mesa.view.*

class SairamAdapter(val toSairam: MutableList<String>, private val itemClick: (String) -> Unit) : RecyclerView.Adapter<SairamAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SairamAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sairam_mesa,parent,false)
        return ViewHolder(view,itemClick)
    }

    override fun getItemCount(): Int {
        return toSairam.size
    }

    override fun onBindViewHolder(holder: SairamAdapter.ViewHolder, position: Int) {
        holder.bindForecast(toSairam[position])
    }

    class ViewHolder(view: View, val itemClick: (String) -> Unit) : RecyclerView.ViewHolder(view) {
        fun bindForecast(membro: String) {
            with(membro) {
                itemView.txtSairam.text = membro
                itemView.setOnClickListener { itemClick(this) }
            }
        }
    }
}
