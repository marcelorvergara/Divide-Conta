package project.inflabnet.mytest.maps

import project.inflabnet.mytest.R
import project.inflabnet.mytest.maps.model.NomesPlacesTraducao
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.place_tipo.view.*

class PlacesAdapter(val listPlace: List<NomesPlacesTraducao>, private val itemClick: (NomesPlacesTraducao) -> Unit):
    RecyclerView.Adapter<PlacesAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.place_tipo, parent, false)
        return ViewHolder(view, itemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindForecast(listPlace[position])
    }

    override fun getItemCount() = listPlace.size

    class ViewHolder(view: View, val itemClick: (NomesPlacesTraducao) -> Unit) : RecyclerView.ViewHolder(view) {

        fun bindForecast(place: NomesPlacesTraducao) {
            with(place) {
                itemView.txtTipo.text = place.nome
                itemView.setOnClickListener { itemClick(this) }
            }
        }
    }
}
