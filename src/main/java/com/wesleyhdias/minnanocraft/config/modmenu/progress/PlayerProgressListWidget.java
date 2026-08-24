package com.wesleyhdias.minnanocraft.config.modmenu.progress;

import com.wesleyhdias.minnanocraft.language.dictionary.DictionaryLoader;
import com.wesleyhdias.minnanocraft.language.resolver.DifficultyResolver;
import com.wesleyhdias.minnanocraft.srs.PlayerVocabularyRepository;
import com.wesleyhdias.minnanocraft.language.dictionary.Word;
import com.wesleyhdias.minnanocraft.srs.models.WordProgress;

import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.Minecraft;

import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;

public class PlayerProgressListWidget extends ObjectSelectionList<PlayerProgressListEntry> {

    private final List<PlayerProgressListEntry> originalEntries = new ArrayList<>();
    private final PlayerVocabularyRepository repository = new PlayerVocabularyRepository();
    private final int listX;
    private final int listWidth;

    public PlayerProgressListWidget(Minecraft minecraft, int width, int height, int x, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
        this.setX(x);
        this.listX = x;
        this.listWidth = width;
        loadWords();
    }

    public enum SortColumn { NONE, WORD, MIDDLE, LEVEL }

    public enum SortDir { NONE, ASC, DESC }

    private void loadWords() {
        ConcurrentHashMap<String, WordProgress> progressMap = repository.loadAll();

        if (progressMap.isEmpty()) {
            this.addEntry(new PlayerProgressListEntry("Nenhuma palavra", "-/-", null, null, this.listX, this.listWidth));
            return;
        }

        for (WordProgress progress : progressMap.values()) {
            String token = progress.getWord();
            int level = progress.getScriptLevel();

            String middleText = null;
            String displayText = token;
            Word word = DictionaryLoader.getDictionary().get(token);
            if (word == null) {
                continue;
            }

            middleText = word.kanji();
            String rendered = DifficultyResolver.render(word, level);
            if (rendered != null && !rendered.isBlank()) {
                displayText = rendered;
            }else{
                displayText = word.translations().getFirst();
            }

            this.addEntry(new PlayerProgressListEntry(
                    displayText,
                    middleText,
                    word,
                    progress,
                    this.listX,
                    this.listWidth
            ));
        }

        this.originalEntries.clear();
        this.originalEntries.addAll(this.children());
    }

    /**
     * Define a largura do "fundo cinza" (caixa de seleção) que aparece quando passamos o mouse.
     * Retornamos a largura total da lista, tirando apenas um espaço de segurança para a barra de rolagem.
     */
    @Override
    public int getRowWidth() {
        return this.listWidth - 12;
    }

    /**
     * Diz ao Minecraft EXATAMENTE onde renderizar a barra de rolagem (Scrollbar).
     * Calculamos o ponto inicial X da lista + a largura total dela, menos a largura da própria barra.
     */
    @Override
    protected int scrollBarX() {
        return this.listX + this.listWidth - 6;
    }

    public void applySorting(SortColumn col, SortDir dir) {
        // Se for NONE, restaura a lista original
        if (dir == SortDir.NONE || col == SortColumn.NONE) {
            this.replaceEntries(this.originalEntries);
            return;
        }

        // Cria uma cópia para ordenar sem estragar a original
        List<PlayerProgressListEntry> sorted = new ArrayList<>(this.originalEntries);

        sorted.sort((a, b) -> {
            int cmp = 0;
            switch (col) {
                case WORD -> cmp = a.getFirstText().compareToIgnoreCase(b.getFirstText());
                case MIDDLE -> {
                    String s1 = a.getMiddleText() == null ? "" : a.getMiddleText();
                    String s2 = b.getMiddleText() == null ? "" : b.getMiddleText();
                    cmp = s1.compareToIgnoreCase(s2);
                }
                case LEVEL -> {
                    cmp = Integer.compare(a.getLevel(), b.getLevel());
                    // Desempata pela exposição se o nível for igual
                    if (cmp == 0) cmp = Double.compare(a.getExposure(), b.getExposure());
                }
            }
            return dir == SortDir.ASC ? cmp : -cmp;
        });

        // Atualiza a tela com a lista ordenada e volta o scroll pro topo
        this.replaceEntries(sorted);
        this.setScrollAmount(0);
    }
}