package br.com.tiker.ui.myPackages

import android.content.Intent
import org.koin.androidx.viewmodel.ext.android.viewModel
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.tiker.R
import br.com.tiker.ui.myPackages.adapter.MyPackagesRecyclerAdapter
import br.com.tiker.ui.stickerPackage.StickerPackageActivity
import br.com.tiker.utils.observe
import kotlinx.android.synthetic.main.activity_sticker_package.*

class MyPackagesFragment : Fragment() {

    companion object {
        fun newInstance() = MyPackagesFragment()
    }

    private val viewModel: MyPackagesViewModel by viewModel()
    private lateinit var adapter: MyPackagesRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_packages, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUi()
    }

    private fun initUi() {
        adapter = MyPackagesRecyclerAdapter()
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = adapter

        adapter.onSelectPackage = {
            startActivity(Intent(context, StickerPackageActivity::class.java).apply {
                putExtra(
                    StickerPackageActivity.STICKER_PACKAGE_ID,
                    it
                )
            })
        }

        adapter.onDeletePackage = {
            viewModel.removeStickerPackage(it)
        }

        observe(viewModel.stickerPackageList) {
            adapter.submitList(it)
        }
    }

}
