package de.emilo.easyclantags.gui;

import de.emilo.easyclantags.model.ClanTag;

final class EditorSession {

    enum Mode { NORMAL, GRADIENT_START, GRADIENT_END }

    ClanTag tag;
    int selected;
    Mode mode = Mode.NORMAL;
    int gradientStart;

    EditorSession(ClanTag tag) {
        this.tag = tag;
    }

    void clampSelection() {
        if (tag.isEmpty()) {
            selected = 0;
        } else {
            selected = Math.max(0, Math.min(selected, tag.length() - 1));
        }
    }
}
