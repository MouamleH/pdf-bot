package me.mouamle.bot.pdf.service;

import java.util.Map;

public interface SnapshotProvider<K, V> {

    String getName();

    Map<K, V> snapshot();

}
