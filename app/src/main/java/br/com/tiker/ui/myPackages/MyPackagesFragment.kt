package br.com.tiker.ui.myPackages

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.tiker.R
import br.com.tiker.ui.myPackages.adapter.MyPackagesRecyclerAdapter
import br.com.tiker.ui.stickerPackage.StickerPackageActivity
import br.com.tiker.utils.gone
import br.com.tiker.utils.observe
import br.com.tiker.utils.visible
import kotlinx.android.synthetic.main.fragment_my_packages.*
import org.koin.androidx.viewmodel.ext.android.viewModel

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
        initializeAdapter()
        observe(viewModel.isLoading) { isLoading ->
            if (isLoading) progressBar?.visible()
            else progressBar?.gone()
        }

        observe(viewModel.stickerPackageList) {
            if (!::adapter.isInitialized) initializeAdapter()
            adapter.submitList(it)
        }

        observe(viewModel.hasItems) { hasItems ->
            if (!hasItems) emptyStateView?.visible()
            else emptyStateView?.gone()
        }
    }

    private fun initializeAdapter() {
        adapter = MyPackagesRecyclerAdapter()
        recyclerView?.layoutManager = LinearLayoutManager(context)
        recyclerView?.adapter = adapter

        adapter.onDeletePackage = {
            viewModel.removeStickerPackage(it)
        }
        adapter.onSelectPackage = {
            startActivity(Intent(context, StickerPackageActivity::class.java).apply {
                putExtra(
                        StickerPackageActivity.STICKER_PACKAGE_ID,
                        it
                )
            })
        }
    }

    override fun onStart() {
        super.onStart()
        if (::adapter.isInitialized) {
            viewModel.forceRechargePackage()
        } else {
            initializeAdapter()
        }
    }

}
