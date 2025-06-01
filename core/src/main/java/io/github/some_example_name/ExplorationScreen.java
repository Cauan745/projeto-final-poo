package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation; // Importar Interpolação
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class ExplorationScreen extends ScreenAdapter {
    final MyGdxGame game;
    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture playerTexture;
    private Texture floorTexture;
    private Texture wallTexture;
    private Texture enemyMapIconTexture;

    // Variáveis para animação do jogador
    private float playerVisualX;          // Posição X visual atual do jogador na tela
    private float playerVisualY;          // Posição Y visual atual do jogador na tela
    private int targetGridX;              // Posição X do grid para onde o jogador está se movendo
    private int targetGridY;              // Posição Y do grid para onde o jogador está se movendo
    private boolean isMoving = false;     // Flag para indicar se o jogador está se movendo
    private float animationTimer = 0f;    // Timer para controlar a duração da animação
    private static final float MOVEMENT_ANIMATION_SPEED = 0.15f; // Duração da animação em segundos (mais rápido)

    public ExplorationScreen(final MyGdxGame game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FitViewport(MyGdxGame.MAP_WIDTH_TILES * MyGdxGame.TILE_SIZE,
                MyGdxGame.MAP_HEIGHT_TILES * MyGdxGame.TILE_SIZE,
                camera);
        camera.setToOrtho(false, viewport.getWorldWidth(), viewport.getWorldHeight());

        playerTexture = game.assetManager.get("player_topdown.png", Texture.class);
        floorTexture = game.assetManager.get("floor.png", Texture.class);
        wallTexture = game.assetManager.get("wall.png", Texture.class);
        enemyMapIconTexture = game.assetManager.get("enemy.png", Texture.class);

        // Inicializar a posição visual do jogador baseada na posição lógica do grid
        this.playerVisualX = game.playerGridX * MyGdxGame.TILE_SIZE;
        this.playerVisualY = game.playerGridY * MyGdxGame.TILE_SIZE;
        this.targetGridX = game.playerGridX;
        this.targetGridY = game.playerGridY;
    }

    private void handleInput(float delta) {
        if (isMoving) { // Se já estiver se movendo, não processar nova entrada
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            tryMove(0, 1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            tryMove(0, -1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            tryMove(-1, 0);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            tryMove(1, 0);
        }
    }

    private void tryMove(int dx, int dy) {
        if (isMoving) return; // Segurança extra

        int nextLogicalX = game.playerGridX + dx;
        int nextLogicalY = game.playerGridY + dy;

        // Checa limites do mapa
        if (nextLogicalX >= 0 && nextLogicalX < MyGdxGame.MAP_WIDTH_TILES &&
            nextLogicalY >= 0 && nextLogicalY < MyGdxGame.MAP_HEIGHT_TILES &&
            game.mapData[MyGdxGame.MAP_HEIGHT_TILES - 1 - nextLogicalY][nextLogicalX] != 1) { // Checa se não é parede

            // Inicia a animação
            isMoving = true;
            targetGridX = nextLogicalX; // Define o alvo lógico
            targetGridY = nextLogicalY;
            animationTimer = 0f;        // Reseta o timer da animação

            // A posição lógica (game.playerGridX/Y) SÓ será atualizada ao final da animação.
        }
    }

    private void updatePlayerMovement(float delta) {
        if (isMoving) {
            animationTimer += delta;
            float progress = Math.min(1f, animationTimer / MOVEMENT_ANIMATION_SPEED);

            // Posição inicial da animação (baseada na posição lógica atual do grid)
            float startScreenX = game.playerGridX * MyGdxGame.TILE_SIZE;
            float startScreenY = game.playerGridY * MyGdxGame.TILE_SIZE;

            // Posição final da animação (baseada no targetGridX/Y)
            float targetScreenX = targetGridX * MyGdxGame.TILE_SIZE;
            float targetScreenY = targetGridY * MyGdxGame.TILE_SIZE;

            // Interpolação linear para suavizar o movimento
            // playerVisualX = startScreenX + (targetScreenX - startScreenX) * progress;
            // playerVisualY = startScreenY + (targetScreenY - startScreenY) * progress;
            // Usar uma interpolação mais suave (opcional, mas fica melhor)
            playerVisualX = Interpolation.sineOut.apply(startScreenX, targetScreenX, progress);
            playerVisualY = Interpolation.sineOut.apply(startScreenY, targetScreenY, progress);


            if (progress >= 1f) {
                isMoving = false;
                // Atualiza a posição lógica do jogador para a posição alvo
                game.playerGridX = targetGridX;
                game.playerGridY = targetGridY;

                // Garante que a posição visual seja exatamente a do tile alvo
                playerVisualX = targetGridX * MyGdxGame.TILE_SIZE;
                playerVisualY = targetGridY * MyGdxGame.TILE_SIZE;

                // Checa se encontrou o inimigo APÓS o movimento terminar
                if (game.mapData[MyGdxGame.MAP_HEIGHT_TILES - 1 - game.playerGridY][game.playerGridX] == 2) {
                    System.out.println("Inimigo encontrado em: " + game.playerGridX + "," + game.playerGridY);
                    game.enemyHP = 50;
                    game.enemyDefeated = false;
                    game.lastAnswerCorrect = false;
                    game.setScreen(game.combatScreen);
                }
            }
        } else {
            // Garante que a posição visual esteja sincronizada com a lógica quando não está movendo
            playerVisualX = game.playerGridX * MyGdxGame.TILE_SIZE;
            playerVisualY = game.playerGridY * MyGdxGame.TILE_SIZE;
        }
    }


    @Override
    public void show() {
        Gdx.input.setInputProcessor(null); // Limpa qualquer input processor anterior
        // Atualiza a posição visual para a posição lógica atual do jogador ao mostrar a tela
        // Isso é importante se você voltar para esta tela e a posição do jogador mudou
        playerVisualX = game.playerGridX * MyGdxGame.TILE_SIZE;
        playerVisualY = game.playerGridY * MyGdxGame.TILE_SIZE;
        targetGridX = game.playerGridX;
        targetGridY = game.playerGridY;
        isMoving = false; // Garante que não esteja em movimento ao mostrar a tela

        if (game.enemyDefeated && game.mapData[MyGdxGame.MAP_HEIGHT_TILES - 1 - game.enemyMapGridY][game.enemyMapGridX] == 2) {
            game.mapData[MyGdxGame.MAP_HEIGHT_TILES - 1 - game.enemyMapGridY][game.enemyMapGridX] = 0;
            // game.enemyDefeated = false; // Removido daqui, pois a flag enemyDefeated é resetada ao entrar em combate
        }
    }

    @Override
    public void render(float delta) {
        handleInput(delta);       // Processa entrada do jogador
        updatePlayerMovement(delta); // Atualiza a lógica de animação do movimento

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();

        // Renderizar o mapa
        for (int y = 0; y < MyGdxGame.MAP_HEIGHT_TILES; y++) {
            for (int x = 0; x < MyGdxGame.MAP_WIDTH_TILES; x++) {
                Texture toDraw = null;
                int tileType = game.mapData[y][x];
                float screenX = x * MyGdxGame.TILE_SIZE;
                float screenY = (MyGdxGame.MAP_HEIGHT_TILES - 1 - y) * MyGdxGame.TILE_SIZE;

                if (tileType == 0) {
                    toDraw = floorTexture;
                } else if (tileType == 1) {
                    toDraw = wallTexture;
                } else if (tileType == 2) {
                    game.batch.draw(floorTexture, screenX, screenY, MyGdxGame.TILE_SIZE, MyGdxGame.TILE_SIZE);
                    toDraw = enemyMapIconTexture;
                }

                if (toDraw != null) {
                    game.batch.draw(toDraw, screenX, screenY, MyGdxGame.TILE_SIZE, MyGdxGame.TILE_SIZE);
                }
            }
        }

        // Renderizar o jogador usando as coordenadas visuais
        game.batch.draw(playerTexture,
                playerVisualX, // Usar playerVisualX
                playerVisualY, // Usar playerVisualY
                MyGdxGame.TILE_SIZE, MyGdxGame.TILE_SIZE);

        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
    }

    @Override
    public void dispose() {
        // Texturas gerenciadas pelo AssetManager
    }
}
