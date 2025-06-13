package io.github.some_example_name.questions;

// Importe o HashMap
import java.util.HashMap;

// Esta classe representa a estrutura de UMA pergunta e seu estado de revisão.
public class QuestionData {
    public String titulo;
    // ALTERAÇÃO AQUI: De Map para HashMap
    public HashMap<String, String> opcoesResposta;
    public String opcaoCorreta;

    // Campos para o Sistema de Repetição Espaçada (SRS)
    public long nextReviewTimestamp = 0; // Timestamp (em milissegundos) da próxima revisão. 0 = revisar agora.
    public int intervalDays = 1;         // Intervalo em dias para a próxima revisão.

    // Construtor vazio é necessário para a desserialização do JSON
    // Vamos inicializar o HashMap aqui para evitar NullPointerExceptions
    public QuestionData() {
        this.opcoesResposta = new HashMap<>();
    }

    // Sobrescrever o equals e hashCode é uma boa prática para identificar perguntas unicamente pelo título
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestionData that = (QuestionData) o;
        return titulo.equals(that.titulo);
    }

    @Override
    public int hashCode() {
        return titulo.hashCode();
    }
}
