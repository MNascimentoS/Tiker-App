package br.com.tiker.ui.allStickers.adapter

import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import br.com.tiker.R
import br.com.tiker.model.StickerModel
import br.com.tiker.utils.StickerPackValidator.STICKER_SIZE_MAX
import br.com.tiker.utils.alert
import br.com.tiker.utils.gone
import br.com.tiker.utils.visible
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_sticker.view.*
import java.io.File


class StickerRecyclerAdapter :
    PagedListAdapter<StickerModel, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    val selectedStickers = arrayListOf<StickerModel>()
    private var isAnimatedPack: Boolean? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        object : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_sticker, parent, false)
        ) {}

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = getItem(position)
        with(holder.itemView) {
            currentItem?.let {
                currentItem.drawable?.let { drawable ->
                    stickerIMG?.setImageDrawable(drawable)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && drawable is AnimatedImageDrawable) {
                        drawable.start()
                    }
                } ?: run {
                    stickerIMG?.setImageBitmap(currentItem.image)
                }
                if (currentItem.selected) {
                    selectedIMG?.visible()
                } else {
                    selectedIMG?.gone()
                }
            }
            setOnClickListener {
                currentItem?.let {
                    isAnimatedPack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && currentItem.drawable is AnimatedImageDrawable) {
                        if (isAnimatedPack == false) {
                            Toast.makeText(context, context.getString(R.string.error_animated_sticker_list), Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        true
                    } else {
                        if (isAnimatedPack == true && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && currentItem.drawable !is AnimatedImageDrawable) {
                            Toast.makeText(context, context.getString(R.string.error_animated_sticker_list), Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        false
                    }
                    currentItem.selected = !currentItem.selected
                    if (currentItem.selected) {
                        selectedStickers.add(currentItem)
                    } else {
                        selectedStickers.remove(currentItem)
                        if (selectedStickers.isEmpty()) isAnimatedPack = null
                        null
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
