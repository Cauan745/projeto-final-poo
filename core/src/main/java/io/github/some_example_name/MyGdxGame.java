package io.github.some_example_name;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.some_example_name.questions.Question;

import java.io.File;
// import com.badlogic.gdx.scenes.scene2d.ui.Skin; // Se for usar

public class MyGdxGame extends Game {

    public SpriteBatch batch;
    public BitmapFont font;
    public AssetManager assetManager;

    // Telas
    public ExplorationScreen explorationScreen;
    public CombatScreen combatScreen;
    public QuestionScreen questionScreen;

    // Estado simples do jogo para MVP
    public int playerHP = 100;
    public int enemyHP = 50;
    public boolean enemyDefeated = false;
    public Question currentQuestion;
    public boolean lastAnswerCorrect = false;
    public boolean returnedFromQuestionScreenWithWrongAnswer = false;

    // Configurações do Mapa de Exploração
    public static final int TILE_SIZE = 32; // Tamanho de cada tile em pixels
    // NOVAS DIMENSÕES DO MAPA
    public static final int MAP_WIDTH_TILES = 20; // Largura do mapa em tiles
    public static final int MAP_HEIGHT_TILES = 8;  // Altura do mapa em tiles

    // Posição do Jogador no Grid do Mapa (inicial)
    public int playerGridX = 1; // Mantendo a posição inicial perto do canto
    public int playerGridY = 1; // Lembre-se que Y=0 é a linha de baixo no grid do jogador

    // Dados do Mapa (0=chão, 1=parede, 2=inimigo)
    // O array será mapData[MAP_HEIGHT_TILES][MAP_WIDTH_TILES]
    public int[][] mapData = new int[MAP_HEIGHT_TILES][MAP_WIDTH_TILES];

    // Coordenadas do inimigo no mapa para remover após derrotado
    // Vamos colocar o inimigo em uma nova posição no mapa maior
    public int enemyMapGridX = 6;
    public int enemyMapGridY = 3; // Lembre-se que mapData[y][x], então no array será mapData[10][15] (se y cresce pra baixo no array)
                                   // Ou, se enemyMapGridY é a coordenada "visual" (y cresce pra cima),
                                   // a linha no array será MAP_HEIGHT_TILES - 1 - enemyMapGridY

    public MyGdxGame() { // Construtor para inicializar o mapa
        initializeMapData();
    }

    private void initializeMapData() {
        for (int y = 0; y < MAP_HEIGHT_TILES; y++) {
            for (int x = 0; x < MAP_WIDTH_TILES; x++) {
                if (y == 0 || y == MAP_HEIGHT_TILES - 1 || x == 0 || x == MAP_WIDTH_TILES - 1) {
                    mapData[y][x] = 1; // Parede nas bordas
                } else {
                    mapData[y][x] = 0; // Chão no interior
                }
            }
        }

        // Adicionar algumas paredes internas para exemplo (opcional)
        // Lembre-se: mapData[linha][coluna]
        // Linha 5, colunas de 5 a 10
        for (int x = 5; x <= 10; x++) {
            if (x < MAP_WIDTH_TILES -1) mapData[5][x] = 1;
        }
        // Coluna 15, linhas de 8 a 12
        for (int y = 8; y <= 12; y++) {
             if (y < MAP_HEIGHT_TILES -1) mapData[y][15] = 1;
        }

        // Colocar o inimigo (2)
        // A posição do inimigo no array mapData depende de como você interpreta enemyMapGridY.
        // Se enemyMapGridY (e playerGridY) representam a linha no array (onde 0 é o topo), então:
        // mapData[enemyMapGridY][enemyMapGridX] = 2;
        // Se enemyMapGridY (e playerGridY) representam a coordenada visual Y (onde 0 é a base do mapa na tela),
        // então a linha no array é invertida:
        if (enemyMapGridY >= 0 && enemyMapGridY < MAP_HEIGHT_TILES &&
            enemyMapGridX >= 0 && enemyMapGridX < MAP_WIDTH_TILES) {
            // Usando a convenção atual do seu código onde playerGridY é a coordenada "visual" e o array mapData tem y invertido:
            // A lógica em ExplorationScreen para desenhar e checar colisões usa:
            // game.mapData[MyGdxGame.MAP_HEIGHT_TILES - 1 - nextY][nextX]
            // Portanto, para definir o inimigo no mapData usando enemyMapGridX e enemyMapGridY como coordenadas visuais:
            int enemyArrayY = MAP_HEIGHT_TILES - 1 - enemyMapGridY;
            if (enemyArrayY >= 0 && enemyArrayY < MAP_HEIGHT_TILES) { // Checagem extra de bounds para enemyArrayY
                 if (mapData[enemyArrayY][enemyMapGridX] == 0) { // Só coloca inimigo se for chão
                    mapData[enemyArrayY][enemyMapGridX] = 2;
                } else {
                     System.err.println("Aviso: Posição do inimigo (" + enemyMapGridX + "," + enemyMapGridY + ") colide com parede. Inimigo não posicionado.");
                     // Poderia tentar encontrar uma nova posição ou deixar sem inimigo
                }
            } else {
                 System.err.println("Aviso: Coordenada Y do inimigo (" + enemyMapGridY + ") está fora dos limites do array após inversão.");
            }

        } else {
            System.err.println("Aviso: Coordenadas X ou Y do inimigo estão fora dos limites do mapa.");
        }
    }


    @Override
    public void create() {
        // initializeMapData(); // Movido para o construtor para garantir que mapData exista antes que as telas o usem
        batch = new SpriteBatch();
        font = new BitmapFont();
        assetManager = new AssetManager();

        assetManager.load("enemy.png", Texture.class);
        assetManager.load("player_topdown.png", Texture.class);
        assetManager.load("floor.png", Texture.class);
        assetManager.load("wall.png", Texture.class);
        assetManager.load("enemy_map_icon.png", Texture.class);
        assetManager.finishLoading();

        currentQuestion = new Question("Qual a capital da França?", new String[]{"Berlim", "Madri", "Paris"}, 2);

        // As telas são inicializadas após mapData ter sido preenchido
        explorationScreen = new ExplorationScreen(this);
        combatScreen = new CombatScreen(this);
        questionScreen = new QuestionScreen(this);

        this.setScreen(explorationScreen);
    }

    // ... (resto da classe MyGdxGame: métodos de cameFromQuestionScreen, render, dispose) ...
    public boolean cameFromQuestionScreenWithError() {
        return returnedFromQuestionScreenWithWrongAnswer;
    }

    public void setReturnedFromQuestionScreenWithWrongAnswer(boolean value) {
        this.returnedFromQuestionScreenWithWrongAnswer = value;
    }

    public void clearCameFromQuestionScreenFlag() {
        this.returnedFromQuestionScreenWithWrongAnswer = false;
    }


    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        assetManager.dispose();
        if (explorationScreen != null) explorationScreen.dispose();
        if (combatScreen != null) combatScreen.dispose();
        if (questionScreen != null) questionScreen.dispose();
    }
}
