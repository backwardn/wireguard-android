/*
 * Copyright © 2017-2019 WireGuard LLC. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.wireguard.util

import java.util.Comparator

/**
 * A keyed list where all elements are sorted by the comparator returned by `comparator()`
 * applied to their keys.
 */
interface SortedKeyedList<K, E : Keyed<out K>> : KeyedList<K, E> {
    fun comparator(): Comparator<in K>?
    fun firstKey(): K?
    fun keySet(): Set<K>
    fun lastKey(): K?
    fun values(): Collection<E>
}
