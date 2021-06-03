package br.com.tiker.ui.adapter

import android.graphics.drawable.AnimatedImageDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.tiker.R
import br.com.tiker.model.StickerModel
//import io.cubos.r2d2lib.inflate
import kotlinx.android.synthetic.main.item_sticker.view.*


class StickerDefaultRecyclerAdapter :
    RecyclerView.Adapter<StickerDefaultRecyclerAdapter.ViewHolder>() {

    private var stickerList = listOf<StickerModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_sticker, parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = stickerList[position]
        with(holder.itemView) {
            currentItem.drawable?.let { drawable ->
                stickerIMG?.setImageDrawable(drawable)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && drawable is AnimatedImageDrawable) {
                    drawable.start()
                }
            } ?: run {
                stickerIMG?.setImageBitmap(currentItem.image)
            }
        }
    }

    override fun getItemCount(): Int = stickerList.size

    fun updateList(stickerList: List<StickerModel>) {
        this.stickerList = stickerList
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

}
