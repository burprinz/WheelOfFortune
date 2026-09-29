package de.luiskun.logic;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Merkt sich entfernte Einträge (Gewinner, die per "Remove" gelöscht wurden) für Undo/Redo.
 */
public final class RemovalHistory {

    /** Ein entfernter Eintrag samt der Position, an der er stand. */
    public record Removal(int index, String name) {
    }

    private final Deque<Removal> undoStack = new ArrayDeque<>();
    private final Deque<Removal> redoStack = new ArrayDeque<>();

    /** Neue Entfernung: Redo-Verlauf wird verworfen. */
    public void record(Removal removal) {
        undoStack.push(removal);
        redoStack.clear();
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /** @return die letzte Entfernung oder null */
    public Removal popUndo() {
        return undoStack.poll();
    }

    /** @return die zuletzt rückgängig gemachte Entfernung oder null */
    public Removal popRedo() {
        return redoStack.poll();
    }

    public void pushUndo(Removal removal) {
        undoStack.push(removal);
    }

    public void pushRedo(Removal removal) {
        redoStack.push(removal);
    }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
}