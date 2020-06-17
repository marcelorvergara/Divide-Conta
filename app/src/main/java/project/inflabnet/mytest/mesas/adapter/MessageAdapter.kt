package project.inflabnet.mytest.mesas.adapter

import project.inflabnet.mytest.R
import project.inflabnet.mytest.mesas.model.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.message_item.view.*

class MessageAdapter (val messages: ArrayList<Message>, val itemClick: (Message) -> Unit) :
    RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
        return ViewHolder(view, itemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindForecast(messages[position])
    }

    override fun getItemCount() = messages.size

    class ViewHolder(view: View, val itemClick: (Message) -> Unit) : RecyclerView.ViewHolder(view) {

        fun bindForecast(message: Message) {
            with(message) {
                if(message.self == false) {
                    itemView.msgOutrosQuem.text = message.userChat
                    itemView.msgOutros.text = message.text
                    itemView.txtHoraOutros.text = message.hora
                    //itemView.setOnClickListener { itemClick(this) }
                    itemView.msgOutrosQuem.visibility = View.VISIBLE
                    itemView.msgOutros.visibility = View.VISIBLE
                    itemView.msgMeu.visibility = View.GONE
                    itemView.txtMsgMeu.visibility = View.GONE
                    itemView.textView28.visibility = View.VISIBLE
                    itemView.textView27.visibility = View.GONE
                    itemView.txtHoraEu.visibility = View.GONE
                    itemView.txtHoraOutros.visibility = View.VISIBLE
                }else{
                    itemView.msgMeu.text = message.userChat
                    itemView.txtMsgMeu.text = message.text
                    itemView.txtHoraEu.text = message.hora
                    //itemView.setOnClickListener { itemClick(this) }
                    itemView.msgOutrosQuem.visibility = View.GONE
                    itemView.msgOutros.visibility = View.GONE
                    itemView.msgMeu.visibility = View.VISIBLE
                    itemView.txtMsgMeu.visibility = View.VISIBLE
                    itemView.textView28.visibility = View.GONE
                    itemView.textView27.visibility = View.VISIBLE
                    itemView.txtHoraEu.visibility = View.VISIBLE
                    itemView.txtHoraOutros.visibility = View.GONE
            }
            }
        }
    }
}