package com.example.android.architecture.blueprints.beetv.modules.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.android.architecture.blueprints.beetv.R
import com.example.android.architecture.blueprints.beetv.databinding.FragmentRegisterBinding
import com.example.android.architecture.blueprints.beetv.util.Constants
import com.example.android.architecture.blueprints.beetv.util.getViewModelFactory
import com.example.android.architecture.blueprints.beetv.widgets.metro.MetroItemFrameLayout
import com.example.android.architecture.blueprints.beetv.widgets.metro.MetroViewBorderHandler
import com.example.android.architecture.blueprints.beetv.widgets.metro.MetroViewBorderImpl

class RegisterFragment : Fragment() {
    private val viewModel by viewModels<RegisterViewModel> { getViewModelFactory() }
    private lateinit var viewDataBinding: FragmentRegisterBinding
    private lateinit var metroViewBorderImpl: MetroViewBorderImpl
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        viewDataBinding = FragmentRegisterBinding.inflate(inflater, container, false).apply {
            click = ClickProxy(viewModel, this@RegisterFragment)
            viewmodel = viewModel
        }
        return viewDataBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val roundedFrameLayout = FrameLayout(context)
        roundedFrameLayout.clipChildren = false
        metroViewBorderImpl = MetroViewBorderImpl(roundedFrameLayout)
        metroViewBorderImpl.attachTo(viewDataBinding.main)
        metroViewBorderImpl.getViewBorder().addOnFocusChanged(object : MetroViewBorderHandler.FocusListener {
            override fun onFocusChanged(oldFocus: View?, newFocus: View?) {
                metroViewBorderImpl.getView().setTag(newFocus)
                changeBackgroundButton(oldFocus, newFocus)
            }
        })

    }

    private fun changeBackgroundButton(oldView: View?, newView: View?) {

        if (oldView != null) {

            if (oldView is MetroItemFrameLayout) {
                oldView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
                if (oldView.getChildAt(0) is TextView) {
                    (oldView.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                }
            }
        }

        if (newView != null) {

            if (newView is MetroItemFrameLayout) {
                newView.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                if (newView.getChildAt(0) is TextView) {
                    (newView.getChildAt(0) as TextView).setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                }
            }

        }

    }

    public class ClickProxy(val viewModel: RegisterViewModel, val fragment: RegisterFragment) {
        fun login(){
            fragment.findNavController().popBackStack()
        }

        fun register(){
            val action = RegisterFragmentDirections.actionRegisterFragmentDestToHomeFragmentDest(Constants.REGISTER)
            fragment.findNavController().navigate(action)
        }
    }
}