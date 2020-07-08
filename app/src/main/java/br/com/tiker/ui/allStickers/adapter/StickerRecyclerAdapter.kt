package br.com.tiker.ui.allStickers.adapter

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import br.com.tiker.R
import br.com.tiker.model.StickerModel
import io.cubos.r2d2lib.gone
import io.cubos.r2d2lib.inflate
import io.cubos.r2d2lib.visible
import kotlinx.android.synthetic.main.item_sticker.view.*


class StickerRecyclerAdapter() :
    PagedListAdapter<StickerModel, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    val selectedStickers = arrayListOf<StickerModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        object : RecyclerView.ViewHolder(parent.inflate(R.layout.item_sticker)) {}

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = getItem(position)
        with(holder.itemView) {
            currentItem?.let {
                stickerIMG.setImageBitmap(currentItem.image)
                if (currentItem.selected) {
                    selectedIMG?.visible()
                } else {
                    selectedIMG?.gone()
                }
            }
            setOnClickListener {
                currentItem?.let {
                    currentItem.selected = !currentItem.selected
                    if (currentItem.selected) {
                        selectedStickers.add(currentItem)
                    } else {
                        selectedStickers.remove(currentItem)
                    }
                }
                notifyDataSetChanged()
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<StickerModel>() {
            override fun areItemsTheSame(
                oldConcert: StickerModel,
                newConcert: StickerModel
            ) = (oldConcert.id == newConcert.id)

            override fun areContentsTheSame(
                oldConcert: StickerModel,
                newConcert: StickerModel
            ) = oldConcert == newConcert
        }
    }
}
