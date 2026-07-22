package com.wesleyhdias.minnanocraft.data.models;

import java.util.List;

public record Word(List<String> translations, String kanji, String hiragana, String romaji) { }
