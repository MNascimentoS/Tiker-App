package br.com.tiker.ui.myPackages.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import br.com.tiker.R
import br.com.tiker.model.StickerPackageModel
import io.cubos.r2d2lib.inflate
import kotlinx.android.synthetic.main.item_sticker_package.view.*

class MyPackagesRecyclerAdapter :
    PagedListAdapter<StickerPackageModel, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    var onSelectPackage: ((Int) -> Unit)? = null
    var onDeletePackage: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        object : RecyclerView.ViewHolder(parent.inflate(R.layout.item_sticker_package)) {}

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