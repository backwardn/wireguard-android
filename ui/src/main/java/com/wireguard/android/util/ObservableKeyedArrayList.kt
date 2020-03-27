/*
 * Copyright © 2017-2019 WireGuard LLC. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.wireguard.android.util

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import com.wireguard.util.Keyed

/**
 * ArrayList that allows looking up elements by some key property. As the key property must always
 * be retrievable, this list cannot hold `null` elements. Because this class places no
 * restrictions on the order or duplication of keys, lookup by key, as well as all list modification
 * operations, require O(n) time.
 */
open class ObservableKeyedArrayList<K, E : Keyed<out K>> : ObservableArrayList<E>(), ObservableKeyedList<K, E> {
    override fun containsAllKeys(keys: Collection<K>): Boolean {
        for (key in keys) {
            if (!containsKey(key))
                return false
        }
        return true
    }

    override fun containsKey(key: K) = indexOfKey(key) >= 0

    override fun get(key: K): E? {
        val index = indexOfKey(key)
        return if (index >= 0) get(index) else null
    }

    override fun getLast(key: K): E? {
        val index = lastIndexOfKey(key)
        return if (index >= 0) get(index) else null
    }

    override fun indexOfKey(key: K): Int {
        val iterator = listIterator()
        while (iterator.hasNext()) {
            val index = iterator.nextIndex()
            if (iterator.next()!!.key == key)
                return index
        }
        return -1
    }

    override fun lastIndexOfKey(key: K): Int {
        val iterator = listIterator(size)
        while (iterator.hasPrevious()) {
            val index = iterator.previousIndex()
            if (iterator.previous()!!.key == key)
                return index
        }
        return -1
    }
}
