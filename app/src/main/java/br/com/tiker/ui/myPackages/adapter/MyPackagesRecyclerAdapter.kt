package br.com.tiker.ui.myPackages.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.AnimatedImageDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import br.com.tiker.R
import br.com.tiker.model.StickerPackageModel
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_sticker.view.*
import kotlinx.android.synthetic.main.item_sticker_package.view.*

class MyPackagesRecyclerAdapter :
    PagedListAdapter<StickerPackageModel, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    var onSelectPackage: ((Int) -> Unit)? = null
    var onDeletePackage: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        object : RecyclerView.ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_sticker_package, parent, false)
        ) {}

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val stickerPackage: StickerPackageModel? = getItem(position)
        with(holder.itemView) {
            stickerPackage?.let {
                packageNameTXT?.text = stickerPackage.name
                packageAuthorTXT?.text = stickerPackage.author
                removePackageBTN?.setOnClickListener {
                    onDeletePackage?.invoke(stickerPackage.id)
                }
                setOnClickListener {
                    onSelectPackage?.invoke(stickerPackage.id)
                }
                stickerPackage.stickerList.size.takeIf { size -> size > 0 }?.let {
                    stickerPackage.stickerList[0].drawable?.let { drawable ->
                        stickerIMG1?.setImageDrawable(drawable)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && drawable is AnimatedImageDrawable) {
                            drawable.start()
                        }
                    } ?: run {
                        stickerIMG1.setImageBitmap(stickerPackage.stickerList[0].image)
                    }
                }
                stickerPackage.stickerList.size.takeIf { size -> size > 1 }?.let {
                    stickerPackage.stickerList[1].drawable?.let { drawable ->
                        stickerIMG2?.setImageDrawable(drawable)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && drawable is AnimatedImageDrawable) {
                            drawable.start()
                        }
                    } ?: run {
                        stickerIMG2.setImageBitmap(stickerPackage.stickerList[1].image)
                    }
                }
                stickerPackage.stickerList.size.takeIf { size -> size > 2 }?.let {
                    stickerPackage.stickerList[2].drawable?.let { drawable ->
                        stickerIMG3?.setImageDrawable(drawable)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && drawable is AnimatedImageDrawable) {
                            drawable.start()
                        }
                    } ?: run {
                        stickerIMG3.setImageBitmap(stickerPackage.stickerList[2].image)
                    }
                }
                stickerPackage.stickerList.size.takeIf { size -> size > 3 }?.let {
                    stickerPackage.stickerList[3].drawable?.let { drawable ->
                        stickerIMG4?.setImageDrawable(drawable)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && drawable is AnimatedImageDrawable) {
                            drawable.start()
                        }
                    } ?: run {
                        stickerIMG4.setImageBitmap(stickerPackage.stickerList[3].image)
                    }
                }
                stickerPackage.stickerList.size.takeIf { size -> size > 4 }?.let {
                    stickerPackage.stickerList[4].drawable?.let { drawable ->
                        stickerIMG5?.setImageDrawable(drawable)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && drawable is AnimatedImageDrawable) {
                            drawable.start()
                        }
                    } ?: run {
                        stickerIMG5.setImageBitmap(stickerPackage.stickerList[4].image)
                    }
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<StickerPackageModel>() {
            override fun areItemsTheSame(
                old: StickerPackageModel,
                new: StickerPackageModel
            ) =
                old == new

            override fun areContentsTheSame(
                old: StickerPackageModel,
                new: StickerPackageModel
            ) = old == new
        }
    }
}