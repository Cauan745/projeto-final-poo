package io.github.some_example_name.questions;

import java.util.ArrayList;

// Esta classe representa o objeto raiz do JSON, que contém a lista de perguntas.
public class QuestionList {
    public ArrayList<QuestionData> perguntas;

    // Construtor vazio é necessário para a desserialização do JSON
    public QuestionList() {}
}
