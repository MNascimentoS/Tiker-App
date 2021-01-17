package br.com.tiker.ui.create

import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import br.com.tiker.R
import br.com.tiker.ui.adapter.StickerDefaultRecyclerAdapter
import br.com.tiker.ui.stickerPackage.StickerPackageActivity
import br.com.tiker.utils.observe
import io.cubos.r2d2lib.CubosActivity
import io.cubos.r2d2lib.gone
import io.cubos.r2d2lib.visible
import kotlinx.android.synthetic.main.activity_create.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateActivity : CubosActivity() {

    private val viewModel: CreateViewModel by viewModel()
    private lateinit var adapter: StickerDefaultRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)
        initUi()
        initListeners()
    }

    private fun initUi() {
        adapter = StickerDefaultRecyclerAdapter()

        recyclerView?.layoutManager = GridLayoutManager(this, 4)
        recyclerView?.adapter = adapter
        observe(viewModel.isLoading) { isLoading ->
            if (isLoading) progressBar?.visible()
            else progressBar?.gone()
        }

        observe(viewModel.stickerList) {
            adapter.updateList(it)
            adapter.notifyDataSetChanged()
        }

        observe(viewModel.callShareActivity) { callShareActivity ->
            if (callShareActivity) {
                viewModel.callShareActivity.value = false
                startActivity(Intent(this, StickerPackageActivity::class.java).apply {
                    putExtra(
                        StickerPackageActivity.STICKER_PACKAGE_ID,
                        viewModel.stickerPackageId
                    )
                })
                finish()
            }
        }


        viewModel.loadUnsavedStickerList()
    }

    private fun initListeners() {
        createPackageBTN?.setOnClickListener {
            val name = packageNameEDT?.text?.toString()
            val author = packageCreatorEDT?.text?.toString()

            when {
                name.isNullOrBlank() -> toastLong(getString(R.string.error_field_name_empty))
                author.isNullOrBlank() -> toastLong(getString(R.string.error_field_author_empty))
                else -> viewModel.saveStickerPackage(name, author)
            }
        }
    }

}
