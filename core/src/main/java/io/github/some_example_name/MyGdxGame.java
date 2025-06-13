package io.github.some_example_name;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import io.github.some_example_name.questions.Question;
import io.github.some_example_name.questions.QuestionData;
import io.github.some_example_name.questions.QuestionList;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class MyGdxGame extends Game {

    public SpriteBatch batch;
    public BitmapFont font;
    public AssetManager assetManager;

    // Telas
    public ExplorationScreen explorationScreen;
    public CombatScreen combatScreen;
    public QuestionScreen questionScreen;

    // Estado do jogo
    public int playerHP = 100;
    public int enemyHP = 50;
    public boolean enemyDefeated = false;
    public Question currentQuestion; // Pergunta formatada para a tela
    public QuestionData currentQuestionData; // Pergunta com dados de SRS
    public boolean lastAnswerCorrect = false;
    public boolean returnedFromQuestionScreenWithWrongAnswer = false;

    // --- Sistema de Perguntas e Repetição Espaçada ---
    private List<QuestionData> allQuestions; // Lista de todas as perguntas em memória
    private Random random;
    // O caminho relativo para o arquivo de perguntas. Será usado tanto em 'internal' quanto em 'local'.
    private final String QUESTIONS_FILE_PATH = "questions/perguntas.json";
    // -------------------------------------------------

    // Configurações do Mapa
    public static final int TILE_SIZE = 32;
    public static final int MAP_WIDTH_TILES = 20;
    public static final int MAP_HEIGHT_TILES = 8;
    public int playerGridX = 1;
    public int playerGridY = 1;
    public int[][] mapData = new int[MAP_HEIGHT_TILES][MAP_WIDTH_TILES];
    public int enemyMapGridX = 6;
    public int enemyMapGridY = 3;

    public MyGdxGame() {
        initializeMapData();
        random = new Random();
    }

    private void initializeMapData() {
        // Inicializa os dados do mapa (paredes, chão, etc.)
        for (int y = 0; y < MAP_HEIGHT_TILES; y++) {for (int x = 0; x < MAP_WIDTH_TILES; x++) {if (y == 0 || y == MAP_HEIGHT_TILES - 1 || x == 0 || x == MAP_WIDTH_TILES - 1) {mapData[y][x] = 1;} else {mapData[y][x] = 0;}}}
        for (int x = 5; x <= 10; x++) {if (x < MAP_WIDTH_TILES -1) mapData[5][x] = 1;}
        for (int y = 8; y <= 12; y++) {if (y < MAP_HEIGHT_TILES -1) mapData[y][15] = 1;}
        if (enemyMapGridY >= 0 && enemyMapGridY < MAP_HEIGHT_TILES && enemyMapGridX >= 0 && enemyMapGridX < MAP_WIDTH_TILES) {int enemyArrayY = MAP_HEIGHT_TILES - 1 - enemyMapGridY; if (enemyArrayY >= 0 && enemyArrayY < MAP_HEIGHT_TILES) {if (mapData[enemyArrayY][enemyMapGridX] == 0) {mapData[enemyArrayY][enemyMapGridX] = 2;} else {System.err.println("Aviso: Posição do inimigo (" + enemyMapGridX + "," + enemyMapGridY + ") colide com parede. Inimigo não posicionado.");}} else {System.err.println("Aviso: Coordenada Y do inimigo (" + enemyMapGridY + ") está fora dos limites do array após inversão.");}} else {System.err.println("Aviso: Coordenadas X ou Y do inimigo estão fora dos limites do mapa.");}
    }


    /**
     * Carrega as perguntas. Se for a primeira vez, copia o arquivo de 'assets' para 'local'.
     * Em todas as execuções subsequentes, carrega diretamente do arquivo 'local'.
     */
    private void loadQuestions() {
        Json json = new Json();
        FileHandle localFile = Gdx.files.local(QUESTIONS_FILE_PATH);

        if (!localFile.exists()) {
            Gdx.app.log("QuestionLoader", "Arquivo de progresso não encontrado. Copiando do 'assets' para 'local'.");
            Gdx.app.log("QuestionLoader", "O arquivo de save será criado em: " + localFile.file().getAbsolutePath());

            // Pega o arquivo mestre da pasta de recursos (read-only)
            FileHandle masterFile = Gdx.files.internal(QUESTIONS_FILE_PATH);
            if (masterFile.exists()) {
                // Copia o arquivo mestre para a pasta local (read/write)
                masterFile.copyTo(localFile);
            } else {
                Gdx.app.error("QuestionLoader", "FATAL: Arquivo mestre 'assets/" + QUESTIONS_FILE_PATH + "' não foi encontrado!");
                allQuestions = new ArrayList<>();
                return;
            }
        }

        // A partir daqui, sempre carregamos o arquivo da pasta 'local'
        Gdx.app.log("QuestionLoader", "Carregando perguntas de: " + localFile.path());
        QuestionList listContainer = json.fromJson(QuestionList.class, localFile);

        if (listContainer != null && listContainer.perguntas != null) {
            allQuestions = listContainer.perguntas;
        } else {
            Gdx.app.error("QuestionLoader", "Erro ao ler o arquivo de perguntas. Criando lista vazia.");
            allQuestions = new ArrayList<>();
        }
        Gdx.app.log("QuestionLoader", "Total de " + allQuestions.size() + " perguntas em memória.");
    }

    /**
     * Salva o estado atual da lista de perguntas (com o progresso da repetição espaçada)
     * de volta para o arquivo na pasta 'local', sobrescrevendo-o.
     */
    private void saveQuestions() {
        if (allQuestions == null || allQuestions.isEmpty()) {
            return;
        }
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json); // Formata o JSON para ser legível

        QuestionList listToSave = new QuestionList();
        listToSave.perguntas = new ArrayList<>(allQuestions);

        FileHandle localFile = Gdx.files.local(QUESTIONS_FILE_PATH);
        localFile.writeString(json.prettyPrint(listToSave), false); // 'false' para sobrescrever
        Gdx.app.log("QuestionSaver", "Progresso salvo em: " + localFile.path());
    }

    /**
     * Seleciona uma pergunta para o combate, priorizando as que estão "vencidas"
     * de acordo com o sistema de repetição espaçada.
     */
    public void prepareNewQuestion() {
        if (allQuestions == null || allQuestions.isEmpty()) {
            // Cria uma pergunta de fallback para evitar que o jogo quebre
            currentQuestionData = new QuestionData();
            currentQuestionData.titulo = "Erro: Nenhuma pergunta disponível.";
            currentQuestion = convertToDisplayQuestion(currentQuestionData);
            return;
        }

        long currentTime = System.currentTimeMillis();

        // Encontra todas as perguntas que estão "vencidas" para revisão
        List<QuestionData> dueQuestions = allQuestions.stream()
            .filter(q -> q.nextReviewTimestamp <= currentTime)
            .collect(Collectors.toList());

        if (!dueQuestions.isEmpty()) {
            // Se há perguntas vencidas, sorteia uma delas
            currentQuestionData = dueQuestions.get(random.nextInt(dueQuestions.size()));
            Gdx.app.log("SRS", "Sorteada pergunta vencida: " + currentQuestionData.titulo);
        } else {
            // Se não há nenhuma vencida, sorteia qualquer uma (para o jogo não parar)
            currentQuestionData = allQuestions.get(random.nextInt(allQuestions.size()));
            Gdx.app.log("SRS", "Nenhuma pergunta vencida. Sorteada pergunta aleatória: " + currentQuestionData.titulo);
        }

        // Converte o objeto de dados para um objeto de exibição para a tela
        currentQuestion = convertToDisplayQuestion(currentQuestionData);
    }

    /**
     * Converte um objeto QuestionData (com dados de SRS) para um objeto Question (para a UI).
     */
    private Question convertToDisplayQuestion(QuestionData data) {
        String[] options = new String[4];
        if (data.opcoesResposta != null) {
            options[0] = data.opcoesResposta.get("a");
            options[1] = data.opcoesResposta.get("b");
            options[2] = data.opcoesResposta.get("c");
            options[3] = data.opcoesResposta.get("d");
        }
        int correctIndex = -1;
        if (data.opcaoCorreta != null) {
            switch (data.opcaoCorreta) {
                case "a": correctIndex = 0; break;
                case "b": correctIndex = 1; break;
                case "c": correctIndex = 2; break;
                case "d": correctIndex = 3; break;
            }
        }
        return new Question(data.titulo, options, correctIndex);
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        assetManager = new AssetManager();

        // Carrega os assets gráficos do jogo
        assetManager.load("enemy.png", Texture.class);
        assetManager.load("player_topdown.png", Texture.class);
        assetManager.load("floor.png", Texture.class);
        assetManager.load("wall.png", Texture.class);
        assetManager.load("enemy_map_icon.png", Texture.class);
        assetManager.finishLoading();

        // Carrega as perguntas usando a lógica híbrida
        loadQuestions();

        // Inicializa as telas do jogo
        explorationScreen = new ExplorationScreen(this);
        combatScreen = new CombatScreen(this);
        questionScreen = new QuestionScreen(this);

        this.setScreen(explorationScreen);
    }

    @Override
    public void dispose() {
        // Salva o progresso das perguntas ao fechar o jogo
        saveQuestions();

        // Libera os recursos
        batch.dispose();
        font.dispose();
        assetManager.dispose();
        if (explorationScreen != null) explorationScreen.dispose();
        if (combatScreen != null) combatScreen.dispose();
        if (questionScreen != null) questionScreen.dispose();
    }

    @Override public void render() {super.render();}
    public boolean cameFromQuestionScreenWithError() {return returnedFromQuestionScreenWithWrongAnswer;}
    public void setReturnedFromQuestionScreenWithWrongAnswer(boolean value) {this.returnedFromQuestionScreenWithWrongAnswer = value;}
    public void clearCameFromQuestionScreenFlag() {this.returnedFromQuestionScreenWithWrongAnswer = false;}
}
