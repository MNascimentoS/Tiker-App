package br.com.tiker.ui.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class TutorialFragment : Fragment() {

    private var layout : Int = 0

    companion object {
        private const val LAYOUT = "layout"
        fun newInstance(layout: Int) = TutorialFragment().apply { this.layout = layout }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        savedInstanceState?.getInt(LAYOUT)?.let { layout = it }
        return inflater.inflate(layout, container, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(LAYOUT, layout)
        super.onSaveInstanceState(outState)
    }


}