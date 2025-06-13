package io.github.some_example_name.questions;

public class Question {
    public String text;
    public String[] options;
    public int correctAnswerIndex;

    // Adicione este construtor vazio
    public Question() {
    }

    public Question(String text, String[] options, int correctAnswerIndex) {
        this.text = text;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
    }
}
