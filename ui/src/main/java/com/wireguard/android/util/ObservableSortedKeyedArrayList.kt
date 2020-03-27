/*
 * Copyright © 2017-2019 WireGuard LLC. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.wireguard.android.util

import androidx.databinding.ObservableList
import com.wireguard.util.Keyed
import com.wireguard.util.SortedKeyedList
import java.util.AbstractList
import java.util.Collections
import java.util.Comparator
import java.util.NoSuchElementException
import java.util.Spliterator

/**
 * KeyedArrayList that enforces uniqueness and sorted order across the set of keys. This class uses
 * binary search to improve lookup and replacement times to O(log(n)). However, due to the
 * array-based nature of this class, insertion and removal of elements with anything but the largest
 * key still require O(n) time.
 */
open class ObservableSortedKeyedArrayList<K, E : Keyed<out K>> : ObservableKeyedArrayList<K, E>, ObservableSortedKeyedList<K, E> {
    private val comparator: Comparator<in K>?

    @Transient
    private val keyList = KeyList(this)

    constructor() {
        comparator = null
    }

    constructor(comparator: Comparator<in K>?) {
        this.comparator = comparator
    }

    constructor(c: Collection<E>) : this() {
        addAll(c)
    }

    constructor(other: SortedKeyedList<K, E>) : this(other.comparator()) {
        addAll(other)
    }

    override fun add(e: E): Boolean {
        val insertionPoint = getInsertionPoint(e)
        if (insertionPoint < 0) {
            // Skipping insertion is non-destructive if the new and existing objects are the same.
            if (e === get(-insertionPoint - 1)) return false
            throw IllegalArgumentException("Element with same key already exists in list")
        }
        super.add(insertionPoint, e)
        return true
    }

    override fun add(index: Int, e: E) {
        val insertionPoint = getInsertionPoint(e)
        require(insertionPoint >= 0) { "Element with same key already exists in list" }
        if (insertionPoint != index) throw IndexOutOfBoundsException("Wrong index given for element")
        super.add(index, e)
    }

    override fun addAll(c: Collection<E>): Boolean {
        var didChange = false
        for (e in c) {
            if (add(e))
                didChange = true
        }
        return didChange
    }

    override fun addAll(index: Int, c: Collection<E>): Boolean {
        var i = index
        for (e in c)
            add(i++, e)
        return true
    }

    override fun comparator(): Comparator<in K>? {
        return comparator
    }

    override fun firstKey(): K? {
        if (isEmpty())
            throw NoSuchElementException("Empty set")
        return get(0).key
    }

    private fun getInsertionPoint(e: E): Int {
        return if (comparator != null) {
            -Collections.binarySearch(keyList, e.key, comparator) - 1
        } else {
            val list = keyList as List<Comparable<K>>
            -Collections.binarySearch(list, e.key) - 1
        }
    }

    override fun indexOfKey(key: K): Int {
        val index: Int
        index = if (comparator != null) {
            Collections.binarySearch(keyList, key, comparator)
        } else {
            val list = keyList as List<Comparable<K>>
            Collections.binarySearch(list, key)
        }
        return if (index >= 0) index else -1
    }

    override fun keySet(): Set<K> {
        return keyList
    }

    override fun lastIndexOfKey(key: K): Int {
        // There can never be more than one element with the same key in the list.
        return indexOfKey(key)
    }

    override fun lastKey(): K {
        if (isEmpty())
            throw NoSuchElementException("Empty set")
        return get(size - 1).key
    }

    override fun set(index: Int, e: E): E {
        val order: Int
        order = if (comparator != null) {
            comparator.compare(e.key, get(index).key)
        } else {
            val key = e.key as Comparable<K>
            key.compareTo(get(index).key)
        }
        if (order != 0) {
            // Allow replacement if the new key would be inserted adjacent to the replaced element.
            val insertionPoint = getInsertionPoint(e)
            if (insertionPoint < index || insertionPoint > index + 1)
                throw IndexOutOfBoundsException("Wrong index given for element")
        }
        return super.set(index, e)
    }

    override fun values(): Collection<E> {
        return this
    }

    private class KeyList<K, E : Keyed<out K>>(private val list: ObservableSortedKeyedArrayList<K, E>) : AbstractList<K>(), MutableSet<K> {
        override fun get(index: Int): K {
            return list[index].key
        }

        override val size = list.size

        override fun spliterator(): Spliterator<K> {
            return super<MutableSet>.spliterator()
        }
    }
}
