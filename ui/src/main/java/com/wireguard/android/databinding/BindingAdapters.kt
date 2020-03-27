/*
 * Copyright © 2017-2019 WireGuard LLC. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.wireguard.android.databinding

import android.text.InputFilter
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableList
import androidx.databinding.ViewDataBinding
import androidx.databinding.adapters.ListenerUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wireguard.android.BR
import com.wireguard.android.R
import com.wireguard.android.databinding.ObservableKeyedRecyclerViewAdapter.RowConfigurationHandler
import com.wireguard.android.util.ObservableKeyedList
import com.wireguard.android.widget.ToggleSwitch
import com.wireguard.android.widget.ToggleSwitch.OnBeforeCheckedChangeListener
import com.wireguard.config.Attribute
import com.wireguard.config.InetNetwork
import com.wireguard.util.Keyed
import java9.util.Optional

/**
 * Static methods for use by generated code in the Android data binding library.
 */
object BindingAdapters {
    @JvmStatic
    @BindingAdapter("checked")
    fun setChecked(view: ToggleSwitch, checked: Boolean) {
        view.setCheckedInternal(checked)
    }

    @JvmStatic
    @BindingAdapter("filter")
    fun setFilter(view: TextView, filter: InputFilter) {
        view.filters = arrayOf(filter)
    }

    @BindingAdapter("items", "layout")
    fun <E> setItems(view: LinearLayout,
                     oldList: ObservableList<E>?, oldLayoutId: Int,
                     newList: ObservableList<E>?, newLayoutId: Int) {
        if (oldList === newList && oldLayoutId == newLayoutId)
            return
        var listener: ItemChangeListener<E>? = ListenerUtil.getListener(view, R.id.item_change_listener)
        // If the layout changes, any existing listener must be replaced.
        if (listener != null && oldList != null && oldLayoutId != newLayoutId) {
            listener.setList(null)
            listener = null
            // Stop tracking the old listener.
            ListenerUtil.trackListener<Any?>(view, null, R.id.item_change_listener)
        }
        // Avoid adding a listener when there is no new list or layout.
        if (newList == null || newLayoutId == 0)
            return
        if (listener == null) {
            listener = ItemChangeListener(view, newLayoutId)
            ListenerUtil.trackListener(view, listener, R.id.item_change_listener)
        }
        // Either the list changed, or this is an entirely new listener because the layout changed.
        listener.setList(newList)
    }

    @BindingAdapter("items", "layout")
    fun <E> setItems(view: LinearLayout,
                     oldList: Iterable<E>?, oldLayoutId: Int,
                     newList: Iterable<E>?, newLayoutId: Int) {
        if (oldList === newList && oldLayoutId == newLayoutId)
            return
        view.removeAllViews()
        if (newList == null)
            return
        val layoutInflater = LayoutInflater.from(view.context)
        for (item in newList) {
            val binding = DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, newLayoutId, view, false)
            binding.setVariable(BR.collection, newList)
            binding.setVariable(BR.item, item)
            binding.executePendingBindings()
            view.addView(binding.root)
        }
    }

    @JvmStatic
    @BindingAdapter(requireAll = false, value = ["items", "layout", "configurationHandler"])
    fun <K, E : Keyed<out K>> setItems(view: RecyclerView,
                                        oldList: ObservableKeyedList<K, E>?, oldLayoutId: Int,
                                        oldRowConfigurationHandler: RowConfigurationHandler<*, *>?,
                                        newList: ObservableKeyedList<K, E>?, newLayoutId: Int,
                                        newRowConfigurationHandler: RowConfigurationHandler<*, *>?) {
        if (view.layoutManager == null)
            view.layoutManager = LinearLayoutManager(view.context, RecyclerView.VERTICAL, false)
        if (oldList === newList && oldLayoutId == newLayoutId)
            return
        // The ListAdapter interface is not generic, so this cannot be checked.
        var adapter = view.adapter as ObservableKeyedRecyclerViewAdapter<K, E>?
        // If the layout changes, any existing adapter must be replaced.
        if (adapter != null && oldList != null && oldLayoutId != newLayoutId) {
            adapter.setList(null)
            adapter = null
        }
        // Avoid setting an adapter when there is no new list or layout.
        if (newList == null || newLayoutId == 0)
            return
        if (adapter == null) {
            adapter = ObservableKeyedRecyclerViewAdapter(view.context, newLayoutId, newList)
            view.adapter = adapter
        }
        adapter.setRowConfigurationHandler(newRowConfigurationHandler)
        // Either the list changed, or this is an entirely new listener because the layout changed.
        adapter.setList(newList)
    }

    @JvmStatic
    @BindingAdapter("onBeforeCheckedChanged")
    fun setOnBeforeCheckedChanged(view: ToggleSwitch,
                                  listener: OnBeforeCheckedChangeListener?) {
        view.setOnBeforeCheckedChangeListener(listener)
    }

    @BindingAdapter("android:text")
    fun setText(view: TextView, text: Optional<*>) {
        view.text = text.map { obj: Any -> obj.toString() }.orElse("")
    }

    @BindingAdapter("android:text")
    fun setText(view: TextView, networks: Iterable<InetNetwork?>?) {
        view.text = if (networks != null) Attribute.join(networks) else ""
    }
}
