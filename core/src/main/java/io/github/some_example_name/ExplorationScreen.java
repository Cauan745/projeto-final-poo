package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils; // Importar MathUtils
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.graphics.Color;

public class ExplorationScreen extends ScreenAdapter {
    final MyGdxGame game;
    private OrthographicCamera camera;
    private Viewport viewport;

    private Texture playerTexture;
    private Texture floorTexture;
    private Texture wallTexture;
    private Texture enemyMapIconTexture;

    private float playerVisualX;
    private float playerVisualY;
    private int targetGridX;
    private int targetGridY;
    private boolean isMoving = false;
    private float animationTimer = 0f;
    private static final float MOVEMENT_ANIMATION_SPEED = 0.15f;

    private static final float EXPLORATION_SPRITE_SCALE_FACTOR = 1f;
    private static final float RENDERED_PLAYER_SIZE = MyGdxGame.TILE_SIZE * EXPLORATION_SPRITE_SCALE_FACTOR;
    private static final float RENDERED_ENEMY_ICON_SIZE = MyGdxGame.TILE_SIZE * EXPLORATION_SPRITE_SCALE_FACTOR;

    // NOVO: Definir quantos tiles você quer que sejam visíveis na tela
    public static final float VIEWPORT_WIDTH_TILES = 10f;  // Ex: 15 tiles de largura
    public static final float VIEWPORT_HEIGHT_TILES = 8f; // Ex: 10 tiles de altura

    private boolean playerFacingRight = true;

    // NOVO: Configurações da borda
    private static final Color BORDER_COLOR = Color.BLACK; // Cor da borda
    private static final float BORDER_THICKNESS = 1.5f;   // Espessura da borda em pixels (ajuste conforme necessário)

    public ExplorationScreen(final MyGdxGame game) {
        this.game = game;

        camera = new OrthographicCamera();
        // Configurar o viewport para o tamanho de visão desejado em pixels
        float viewportWidthPixels = VIEWPORT_WIDTH_TILES * MyGdxGame.TILE_SIZE;
        float viewportHeightPixels = VIEWPORT_HEIGHT_TILES * MyGdxGame.TILE_SIZE;

        viewport = new FitViewport(viewportWidthPixels, viewportHeightPixels, camera);
        // Inicialmente, a câmera pode ser centralizada no viewport,
        // mas ela será atualizada para seguir o jogador.
        // camera.setToOrtho(false, viewport.getWorldWidth(), viewport.getWorldHeight()); // Y-down
        // Vamos manter o Y-up para a câmera, o desenho do mapa já lida com a inversão.
        camera.setToOrtho(false, viewportWidthPixels, viewportHeightPixels);


        playerTexture = game.assetManager.get("player_topdown.png", Texture.class);
        floorTexture = game.assetManager.get("floor.png", Texture.class);
        wallTexture = game.assetManager.get("wall.png", Texture.class);
        enemyMapIconTexture = game.assetManager.get("enemy.png", Texture.class);

        this.playerVisualX = game.playerGridX * MyGdxGame.TILE_SIZE;
        this.playerVisualY = game.playerGridY * MyGdxGame.TILE_SIZE;
        this.targetGridX = game.playerGridX;
        this.targetGridY = game.playerGridY;

        // Posicionar a câmera inicialmente no jogador
        updateCameraPosition();
    }

    private void updateCameraPosition() {
        // Centralizar a câmera na posição visual ATUAL do jogador
        // Adicionamos TILE_SIZE / 2 para centralizar no meio do tile do jogador
        float cameraX = playerVisualX + (MyGdxGame.TILE_SIZE / 2f);
        float cameraY = playerVisualY + (MyGdxGame.TILE_SIZE / 2f);

        // Limitar a posição da câmera para que não mostre áreas fora do mapa
        // O centro da câmera não pode ir além de ( viewport.getWorldWidth()/2 ) das bordas do mapa.
        float mapPixelWidth = MyGdxGame.MAP_WIDTH_TILES * MyGdxGame.TILE_SIZE;
        float mapPixelHeight = MyGdxGame.MAP_HEIGHT_TILES * MyGdxGame.TILE_SIZE;

        // MathUtils.clamp(valor, min, max)
        camera.position.x = MathUtils.clamp(cameraX, viewport.getWorldWidth() / 2f, mapPixelWidth - viewport.getWorldWidth() / 2f);
        camera.position.y = MathUtils.clamp(cameraY, viewport.getWorldHeight() / 2f, mapPixelHeight - viewport.getWorldHeight() / 2f);

        // Se o mapa for menor que o viewport em alguma dimensão, centralizar naquela dimensão
        if (mapPixelWidth < viewport.getWorldWidth()) {
            camera.position.x = mapPixelWidth / 2f;
        }
        if (mapPixelHeight < viewport.getWorldHeight()) {
            camera.position.y = mapPixelHeight / 2f;
        }

        camera.update();
    }


    private void handleInput(float delta) {
        if (isMoving) {
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) tryMove(0, 1);
        else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) tryMove(0, -1);
        else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) tryMove(-1, 0);
        else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) tryMove(1, 0);
    }

    private void tryMove(int dx, int dy) {
    if (isMoving) return;

    int nextLogicalX = game.playerGridX + dx;
    int nextLogicalY = game.playerGridY + dy;

    // Atualizar a direção do personagem
    if (dx > 0) { // Movendo para a direita
        playerFacingRight = true;
    } else if (dx < 0) { // Movendo para a esquerda
        playerFacingRight = false;
    }
    // Se dx == 0 (movendo para cima ou para baixo), a direção horizontal não muda.

    if (nextLogicalX >= 0 && nextLogicalX < MyGdxGame.MAP_WIDTH_TILES &&
        nextLogicalY >= 0 && nextLogicalY < MyGdxGame.MAP_HEIGHT_TILES &&
        game.mapData[MyGdxGame.MAP_HEIGHT_TILES - 1 - nextLogicalY][nextLogicalX] != 1) {
        isMoving = true;
        targetGridX = nextLogicalX;
        targetGridY = nextLogicalY;
        animationTimer = 0f;
    }
}

    private void updatePlayerMovement(float delta) {
        if (isMoving) {
            animationTimer += delta;
            float progress = Math.min(1f, animationTimer / MOVEMENT_ANIMATION_SPEED);

            float startScreenX = game.playerGridX * MyGdxGame.TILE_SIZE;
            float startScreenY = game.playerGridY * MyGdxGame.TILE_SIZE;
            float targetScreenX = targetGridX * MyGdxGame.TILE_SIZE;
            float targetScreenY = targetGridY * MyGdxGame.TILE_SIZE;

            playerVisualX = Interpolation.sineOut.apply(startScreenX, targetScreenX, progress);
            playerVisualY = Interpolation.sineOut.apply(startScreenY, targetScreenY, progress);

            if (progress >= 1f) {
                isMoving = false;
                game.playerGridX = targetGridX;
                game.playerGridY = targetGridY;
                playerVisualX = targetGridX * MyGdxGame.TILE_SIZE;
                playerVisualY = targetGridY * MyGdxGame.TILE_SIZE;

                if (game.mapData[MyGdxGame.MAP_HEIGHT_TILES - 1 - game.playerGridY][game.playerGridX] == 2) {
                    System.out.println("Inimigo encontrado em: " + game.playerGridX + "," + game.playerGridY);
                    // ... (lógica de combate)
                    game.enemyHP = 50;
                    game.enemyDefeated = false;
                    game.lastAnswerCorrect = false;
                    game.setScreen(game.combatScreen);
                }
            }
        } else {
            playerVisualX = game.playerGridX * MyGdxGame.TILE_SIZE;
            playerVisualY = game.playerGridY * MyGdxGame.TILE_SIZE;
        }
        // Atualizar a câmera APÓS o movimento do jogador ser calculado
        updateCameraPosition();
    }


    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);
        // Sincronizar posição visual e lógica do jogador
        playerVisualX = game.playerGridX * MyGdxGame.TILE_SIZE;
        playerVisualY = game.playerGridY * MyGdxGame.TILE_SIZE;
        targetGridX = game.playerGridX;
        targetGridY = game.playerGridY;
        isMoving = false;

        // Atualizar a posição da câmera quando a tela é mostrada
        updateCameraPosition();

        if (game.enemyDefeated && game.mapData[MyGdxGame.MAP_HEIGHT_TILES - 1 - game.enemyMapGridY][game.enemyMapGridX] == 2) {
            // ... (lógica de remover inimigo)
            int enemyArrayY = MyGdxGame.MAP_HEIGHT_TILES - 1 - game.enemyMapGridY;
             if (enemyArrayY >= 0 && enemyArrayY < MyGdxGame.MAP_HEIGHT_TILES &&
                game.enemyMapGridX >= 0 && game.enemyMapGridX < MyGdxGame.MAP_WIDTH_TILES &&
                game.mapData[enemyArrayY][game.enemyMapGridX] == 2) {
                game.mapData[enemyArrayY][game.enemyMapGridX] = 0;
            }
        }
    }

    // Dentro do método render(float delta) em ExplorationScreen

// Método auxiliar para desenhar a borda
    private void drawWithBorder(Texture texture, float x, float y, float width, float height,
                                boolean flipX, boolean flipY, Color borderColor, float borderThickness) {
        Color originalColor = game.batch.getColor().cpy(); // Salva a cor original do batch
        game.batch.setColor(borderColor); // Define a cor para a borda

        int srcX = 0;
        int srcY = 0;
        int srcWidth = texture.getWidth();
        int srcHeight = texture.getHeight();

        // Desenha as "sombras" da borda
        float[] offsets = {
            -borderThickness, -borderThickness,  // Cima-esquerda
             borderThickness, -borderThickness,  // Cima-direita
            -borderThickness,  borderThickness,  // Baixo-esquerda
             borderThickness,  borderThickness,  // Baixo-direita
            -borderThickness, 0,                 // Esquerda
             borderThickness, 0,                 // Direita
             0, -borderThickness,                // Cima
             0,  borderThickness                 // Baixo
        };

        for (int i = 0; i < offsets.length; i += 2) {
            game.batch.draw(texture, x + offsets[i], y + offsets[i+1], width, height, srcX, srcY, srcWidth, srcHeight, flipX, flipY);
        }

        game.batch.setColor(originalColor); // Restaura a cor original
        // Desenha o sprite principal por cima
        game.batch.draw(texture, x, y, width, height, srcX, srcY, srcWidth, srcHeight, flipX, flipY);
    }

@Override
    public void render(float delta) {
        handleInput(delta);
        updatePlayerMovement(delta);

        game.batch.setProjectionMatrix(camera.combined);

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.begin();

        float playerBaseX = playerVisualX; // Posição base do tile do jogador
        float playerBaseY = playerVisualY;
        float playerRenderOffsetX = (MyGdxGame.TILE_SIZE - RENDERED_PLAYER_SIZE) / 2f; // Offset para centralizar sprite menor
        float playerRenderOffsetY = (MyGdxGame.TILE_SIZE - RENDERED_PLAYER_SIZE) / 2f;

        float enemyIconRenderOffsetX = (MyGdxGame.TILE_SIZE - RENDERED_ENEMY_ICON_SIZE) / 2f;
        float enemyIconRenderOffsetY = (MyGdxGame.TILE_SIZE - RENDERED_ENEMY_ICON_SIZE) / 2f;


        // Renderizar o mapa
        int startX = MathUtils.floor((camera.position.x - viewport.getWorldWidth() / 2f) / MyGdxGame.TILE_SIZE) - 1;
        int endX = MathUtils.ceil((camera.position.x + viewport.getWorldWidth() / 2f) / MyGdxGame.TILE_SIZE) + 1;

        for (int mapY_idx = 0; mapY_idx < MyGdxGame.MAP_HEIGHT_TILES; mapY_idx++) {
            float screenTileY = (MyGdxGame.MAP_HEIGHT_TILES - 1 - mapY_idx) * MyGdxGame.TILE_SIZE;
            if (screenTileY + MyGdxGame.TILE_SIZE < camera.position.y - viewport.getWorldHeight() / 2f ||
                screenTileY > camera.position.y + viewport.getWorldHeight() / 2f) {
                continue;
            }

            for (int mapX_idx = startX; mapX_idx < endX; mapX_idx++) {
                if (mapX_idx < 0 || mapX_idx >= MyGdxGame.MAP_WIDTH_TILES) continue;

                int tileType = game.mapData[mapY_idx][mapX_idx];
                float screenTileX = mapX_idx * MyGdxGame.TILE_SIZE;

                if (tileType == 1) {
                    game.batch.draw(wallTexture, screenTileX, screenTileY, MyGdxGame.TILE_SIZE, MyGdxGame.TILE_SIZE);
                } else {
                    game.batch.draw(floorTexture, screenTileX, screenTileY, MyGdxGame.TILE_SIZE, MyGdxGame.TILE_SIZE);
                    if (tileType == 2) {
                        // Desenhar ícone do inimigo com borda
                        drawWithBorder(enemyMapIconTexture,
                                screenTileX + enemyIconRenderOffsetX,
                                screenTileY + enemyIconRenderOffsetY,
                                RENDERED_ENEMY_ICON_SIZE,
                                RENDERED_ENEMY_ICON_SIZE,
                                false, false, // Ícone do inimigo não vira (pode mudar se quiser)
                                BORDER_COLOR, BORDER_THICKNESS);
                    }
                }
            }
        }

        // Renderizar o jogador com borda
        boolean flipXPlayer = !playerFacingRight;
        drawWithBorder(playerTexture,
                playerBaseX + playerRenderOffsetX, // Posição final de renderização do sprite principal
                playerBaseY + playerRenderOffsetY,
                RENDERED_PLAYER_SIZE,
                RENDERED_PLAYER_SIZE,
                flipXPlayer, false,
                BORDER_COLOR, BORDER_THICKNESS);

        game.batch.end();
    }
    @Override
    public void resize(int width, int height) {
        // Atualiza o viewport, mas a câmera NÃO é centralizada no meio do viewport aqui.
        // A câmera agora segue o jogador.
        viewport.update(width, height, false); // O 'false' para não centralizar a câmera
                                               // ou 'true' se quiser que o viewport tente manter o centro da câmera
                                               // No nosso caso, já gerenciamos a posição da câmera.
                                               // O importante é que o viewport.update atualize suas dimensões de tela.
        // Se você usar 'true' em viewport.update(width, height, true) e a câmera não tiver sido
        // movida ainda (ex: no primeiro frame), ela será centralizada no (0,0) do mundo do viewport.
        // Como estamos chamando updateCameraPosition() no show e no render, está ok.
    }

    @Override
    public void dispose() {
        // Texturas gerenciadas pelo AssetManager
    }
}
