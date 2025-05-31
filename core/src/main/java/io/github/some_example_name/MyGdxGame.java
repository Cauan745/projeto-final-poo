package io.github.some_example_name;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

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
    public boolean enemyDefeated = false; // Para lógica de combate
    public Question currentQuestion;
    public boolean lastAnswerCorrect = false;

    // Configurações do Mapa de Exploração
    public static final int TILE_SIZE = 32; // Tamanho de cada tile em pixels
    public static final int MAP_WIDTH_TILES = 10; // Largura do mapa em tiles
    public static final int MAP_HEIGHT_TILES = 8;  // Altura do mapa em tiles

    // Posição do Jogador no Grid do Mapa (inicial)
    public int playerGridX = 1;
    public int playerGridY = 1;

    // Dados do Mapa (0=chão, 1=parede, 2=inimigo)
    // Mapa simples:
    // W W W W W W W W W W
    // W P . . . . . . . W
    // W . W W . . E . . W
    // W . W . . . . . . W
    // W . . . . . . . . W
    // W . . . W . . . . W
    // W . . . W . . . . W
    // W W W W W W W W W W
    public int[][] mapData = {
        {1,1,1,1,1,1,1,1,1,1},
        {1,0,0,0,0,0,0,0,0,1},
        {1,0,1,1,0,0,2,0,0,1}, // Posição do inimigo (2) em (6,2) da perspectiva do array (x,y)
        {1,0,1,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,1,0,0,0,0,1},
        {1,0,0,0,1,0,0,0,0,1},
        {1,1,1,1,1,1,1,1,1,1}
    };
    // Coordenadas do inimigo no mapa para remover após derrotado
    public int enemyMapGridX = 6;
    public int enemyMapGridY = 2; // Ajustar se mudar no mapData (lembre-se que mapData[y][x])


    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont(); // Use a default ou carregue a sua (ex: new BitmapFont(Gdx.files.internal("font.fnt"));)
        assetManager = new AssetManager();

        // Carregar assets

        assetManager.load("enemy.png", Texture.class); // Para tela de combate
        assetManager.load("player_topdown.png", Texture.class);
        assetManager.load("floor.png", Texture.class);
        assetManager.load("wall.png", Texture.class);
        assetManager.load("enemy_map_icon.png", Texture.class);
        // assetManager.load("uiskin.json", Skin.class); // Se for usar skin para UI
        assetManager.finishLoading(); // Bloqueia até carregar para o MVP

        // Pergunta de exemplo
        currentQuestion = new Question("Qual a capital da França?", new String[]{"Berlim", "Madri", "Paris"}, 2);

        // Inicializa as telas
        explorationScreen = new ExplorationScreen(this);
        combatScreen = new CombatScreen(this); // Passa 'this' (o jogo)
        questionScreen = new QuestionScreen(this); // Passa 'this' (o jogo)


        this.setScreen(explorationScreen);
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
