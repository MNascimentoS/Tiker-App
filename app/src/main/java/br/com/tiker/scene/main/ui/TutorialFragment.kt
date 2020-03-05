package br.com.tiker.scene.main.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlin.properties.Delegates

class TutorialFragment : Fragment() {

    private var layout by Delegates.notNull<Int>()

    companion object {
        fun newInstance(layout: Int) = TutorialFragment().apply { this.layout = layout }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(layout, container, false)

}