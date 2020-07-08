package br.com.tiker.ui.adapter

import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.com.tiker.R
import io.cubos.r2d2lib.inflate
import kotlinx.android.synthetic.main.item_sticker.view.*


class StickerDefaultRecyclerAdapter :
    RecyclerView.Adapter<StickerDefaultRecyclerAdapter.ViewHolder>() {

    private val bitmapList = arrayListOf<Bitmap>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(parent.inflate(R.layout.item_sticker))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = bitmapList[position]
        with(holder.itemView) {
            currentItem.let { stickerIMG.setImageBitmap(currentItem) }
        }
    }

    override fun getItemCount(): Int = bitmapList.size

    fun updateList(bitmapList: List<Bitmap>) {
        this.bitmapList.addAll(bitmapList)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

}
