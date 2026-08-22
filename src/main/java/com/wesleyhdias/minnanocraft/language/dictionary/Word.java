package com.wesleyhdias.minnanocraft.language.dictionary;

import java.util.List;

public record Word(List<String> translations, String kanji, String hiragana, String romaji) { }
