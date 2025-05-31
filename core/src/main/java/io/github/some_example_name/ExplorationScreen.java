package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter; // Usar ScreenAdapter para não precisar implementar todos os métodos
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class ExplorationScreen extends ScreenAdapter { // Herda de ScreenAdapter
    final MyGdxGame game;
    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture playerTexture;
    private Texture floorTexture;
    private Texture wallTexture;
    private Texture enemyMapIconTexture;

    public ExplorationScreen(final MyGdxGame game) {
        this.game = game;

        // Configuração da câmera e viewport para o mundo do jogo
        // Visão do tamanho do mapa em pixels
        camera = new OrthographicCamera();
        viewport = new FitViewport(MyGdxGame.MAP_WIDTH_TILES * MyGdxGame.TILE_SIZE,
            MyGdxGame.MAP_HEIGHT_TILES * MyGdxGame.TILE_SIZE,
            camera);
        camera.setToOrtho(false, viewport.getWorldWidth(), viewport.getWorldHeight()); // Y-down para corresponder ao array

        // Carregar texturas do AssetManager

        playerTexture = game.assetManager.get("player_topdown.png", Texture.class);
        floorTexture = game.assetManager.get("floor.png", Texture.class);
        wallTexture = game.assetManager.get("wall.png", Texture.class);
        //enemyMapIconTexture = game.assetManager.get("enemy_map_icon.png", Texture.class);
        enemyMapIconTexture = game.assetManager.get("enemy.png", Texture.class);
    }

    private void handleInput(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            tryMove(0, 1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            tryMove(0, -1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            tryMove(-1, 0);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            tryMove(1, 0);
        }
    }

    private void tryMove(int dx, int dy) {
        int nextX = game.playerGridX + dx;
        int nextY = game.playerGridY + dy;

        // Checa limites do mapa
        // Atenção: mapData[y][x]
        if (nextX >= 0 && nextX < MyGdxGame.MAP_WIDTH_TILES &&
            nextY >= 0 && nextY < MyGdxGame.MAP_HEIGHT_TILES &&
            game.mapData[MyGdxGame.MAP_HEIGHT_TILES - 1 - nextY][nextX] != 1) { // Checa se não é parede (1)
            // Invertendo Y para o array mapData
            game.playerGridX = nextX;
            game.playerGridY = nextY;

            // Checa se encontrou o inimigo
            // Atenção: mapData[y][x] e as coordenadas do inimigo
            if (game.mapData[MyGdxGame.MAP_HEIGHT_TILES - 1 - game.playerGridY][game.playerGridX] == 2) {
                System.out.println("Inimigo encontrado em: " + game.playerGridX + "," + game.playerGridY);
                // Resetar o estado do combate para um novo encontro
                game.enemyHP = 50;
                game.enemyDefeated = false;
                game.lastAnswerCorrect = false; // Reseta a flag da última resposta
                game.setScreen(game.combatScreen);
            }
        }
    }


    @Override
    public void show() {
        // Quando esta tela é mostrada, resetar a posição do jogador se o inimigo foi derrotado
        // ou para um novo jogo (lógica a ser adicionada se necessário)
        // E remover o ícone do inimigo do mapa se ele foi derrotado
        if (game.enemyDefeated && game.mapData[MyGdxGame.MAP_HEIGHT_TILES - 1 - game.enemyMapGridY][game.enemyMapGridX] == 2) {
            game.mapData[MyGdxGame.MAP_HEIGHT_TILES - 1 - game.enemyMapGridY][game.enemyMapGridX] = 0; // Transforma em chão
            game.enemyDefeated = false; // Reseta para poder encontrar de novo (ou lógica de não respawn)
        }
    }

    @Override
    public void render(float delta) {
        handleInput(delta);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined); // Usa a câmera do jogo

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1); // Cor de fundo
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();

        // Renderizar o mapa
        // mapData está [linha/y][coluna/x]
        // y do mapa cresce para baixo no array, y da tela cresce para cima
        for (int y = 0; y < MyGdxGame.MAP_HEIGHT_TILES; y++) {
            for (int x = 0; x < MyGdxGame.MAP_WIDTH_TILES; x++) {
                Texture toDraw = null;
                int tileType = game.mapData[y][x]; // mapData[linha][coluna]
                float screenX = x * MyGdxGame.TILE_SIZE;
                float screenY = (MyGdxGame.MAP_HEIGHT_TILES - 1 - y) * MyGdxGame.TILE_SIZE; // Inverte Y para desenhar

                if (tileType == 0) { // Chão
                    toDraw = floorTexture;
                } else if (tileType == 1) { // Parede
                    toDraw = wallTexture;
                } else if (tileType == 2) { // Inimigo
                    game.batch.draw(floorTexture, screenX, screenY, MyGdxGame.TILE_SIZE, MyGdxGame.TILE_SIZE); // Chão por baixo
                    toDraw = enemyMapIconTexture; // Ícone do inimigo
                }

                if (toDraw != null) {
                    game.batch.draw(toDraw, screenX, screenY, MyGdxGame.TILE_SIZE, MyGdxGame.TILE_SIZE);
                }
            }
        }

        // Renderizar o jogador
        game.batch.draw(playerTexture,
            game.playerGridX * MyGdxGame.TILE_SIZE,
            game.playerGridY * MyGdxGame.TILE_SIZE,
            MyGdxGame.TILE_SIZE, MyGdxGame.TILE_SIZE);

        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true); // Atualiza o viewport, centraliza a câmera
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
    }

    @Override
    public void dispose() {
        // As texturas são gerenciadas pelo AssetManager, que é disposed na classe MyGdxGame
        // Se você criasse texturas diretamente aqui sem o AssetManager, precisaria dar dispose nelas.
    }
}
