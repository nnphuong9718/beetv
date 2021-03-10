package com.example.android.architecture.blueprints.beetv.common.basegui

import android.content.Context
import android.os.Bundle
import com.example.android.architecture.blueprints.beetv.helpers.AppEvent

import android.view.View;
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

open class BaseFragment : Fragment() {



    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    open fun myOnKeyDown(keyCode: Int) {
    }

    open fun getMyTag(): String {
        return "OriginalTag"
    }

    open fun onBackPress() {
    }


    internal class ViewLifecycleOwner : LifecycleOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)
        override fun getLifecycle(): LifecycleRegistry {
            return lifecycleRegistry
        }
    }

    @Nullable
    private var viewLifecycleOwner: ViewLifecycleOwner? = null

    /**
     * @return the Lifecycle owner of the current view hierarchy,
     * or null if there is no current view hierarchy.
     */
    @Nullable
    open fun getViewLifeCycleOwner(): LifecycleOwner? {
        return viewLifecycleOwner
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner = ViewLifecycleOwner()
        viewLifecycleOwner!!.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onStart() {
        super.onStart()
        if (viewLifecycleOwner != null) {
            viewLifecycleOwner!!.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewLifecycleOwner != null) {
            viewLifecycleOwner!!.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }
    }

    override fun onPause() {
        if (viewLifecycleOwner != null) {
            viewLifecycleOwner!!.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        }
        super.onPause()
    }

    override fun onStop() {
        if (viewLifecycleOwner != null) {
            viewLifecycleOwner!!.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        }
        super.onStop()
    }

    override fun onDestroyView() {
        if (viewLifecycleOwner != null) {
            viewLifecycleOwner!!.lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            viewLifecycleOwner = null
        }
        super.onDestroyView()
    }
}