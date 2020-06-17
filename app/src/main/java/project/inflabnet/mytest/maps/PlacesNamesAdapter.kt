package project.inflabnet.mytest.maps

import project.inflabnet.mytest.R
import project.inflabnet.mytest.maps.model.NomesPlacesEnderecos
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.place_name.view.*

class PlacesNamesAdapter(val toReturn: ArrayList<NomesPlacesEnderecos>, private val itemClick: (NomesPlacesEnderecos) -> Unit) :
    RecyclerView.Adapter<PlacesNamesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.place_name, parent, false)
        return ViewHolder(view, itemClick)
    }

    override fun getItemCount(): Int {
        return toReturn.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindForecast(toReturn[position])
    }

    class ViewHolder(view: View, val itemClick: (NomesPlacesEnderecos) -> Unit) : RecyclerView.ViewHolder(view) {

        fun bindForecast(local: NomesPlacesEnderecos) {
            with(local) {
                itemView.txtNameLocal.text = local.nome
                itemView.txtEndLocal.text = local.endereco
                itemView.txtNotaLocal.text = local.rating.toString()
                itemView.txtPorxiLocal.text = local.proximidade.toString()
                itemView.setOnClickListener { itemClick(this) }
            }
        }
    }
}
