package br.com.tiker.ui.allStickers

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import br.com.tiker.R
import br.com.tiker.ui.allStickers.adapter.StickerRecyclerAdapter
import br.com.tiker.ui.create.CreateActivity
import br.com.tiker.utils.gone
import br.com.tiker.utils.observe
import br.com.tiker.utils.visible
import kotlinx.android.synthetic.main.fragment_all_stickers.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class AllStickersFragment : Fragment() {

    companion object {
        fun newInstance() = AllStickersFragment()
    }

    private var firstLoad: Boolean = true
    private val viewModel: AllStickersViewModel by sharedViewModel()
    private lateinit var adapter: StickerRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_all_stickers, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initUi()
    }

    private fun initUi() {
        initializeAdapter()

        observe(viewModel.stickerList) {
            if (!::adapter.isInitialized) initializeAdapter()
            adapter.submitList(it)
            firstLoad = false
        }

        observe(viewModel.isLoading) { isLoading ->
            if (isLoading) progressBar?.visible()
            else progressBar?.gone()
        }

        observe(viewModel.hasItems) { hasItems ->
            if (!hasItems) emptyStateView?.visible()
        }
    }

    fun createPackage() {
        if (::adapter.isInitialized) {
            if (adapter.selectedStickers.size >= 3) {
                viewModel.saveTempStickerList(adapter.selectedStickers)
                startActivity(Intent(context, CreateActivity::class.java))
            } else {
                Toast.makeText(context, getString(R.string.error_min_stickers), Toast.LENGTH_LONG).show()
            }
        } else {
            initializeAdapter()
        }
    }

    private fun initializeAdapter() {
        adapter = StickerRecyclerAdapter()
        recyclerView?.layoutManager = GridLayoutManager(context, 4)
        recyclerView?.adapter = adapter
    }

}
