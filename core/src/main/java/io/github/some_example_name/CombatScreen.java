package io.github.some_example_name;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class CombatScreen implements Screen {
    final MyGdxGame game;
    private Stage stage;
    private Skin skin;
    private Texture playerTexture;
    private Texture enemyTexture;
    private Label playerHpLabel;
    private Label enemyHpLabel;

    // Novo: Defina o tamanho desejado para os sprites na tela de combate
    // Você pode usar um fator de escala sobre o tamanho original da textura
    // ou definir larguras/alturas fixas.
    private static final float COMBAT_SPRITE_SCALE_FACTOR = 0.8f; // 80% do tamanho original
    // Ou tamanhos fixos:
    // private static final float COMBAT_PLAYER_WIDTH = 50f;
    // private static final float COMBAT_PLAYER_HEIGHT = 70f;
    // private static final float COMBAT_ENEMY_WIDTH = 60f;
    // private static final float COMBAT_ENEMY_HEIGHT = 80f;
    // NOVO: Configurações da borda (pode ser diferente da tela de exploração se quiser)
    private static final Color BORDER_COLOR_COMBAT = Color.BLACK;
    private static final float BORDER_THICKNESS_COMBAT = 2.0f; // Borda um pouco mais grossa para combate?

    public CombatScreen(final MyGdxGame game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());

        playerTexture = game.assetManager.get("player_topdown.png", Texture.class);
        enemyTexture = game.assetManager.get("enemy.png", Texture.class);

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = game.font;

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.font;
        labelStyle.fontColor = Color.WHITE;


        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        playerHpLabel = new Label("Player HP: " + game.playerHP, labelStyle);
        enemyHpLabel = new Label("Enemy HP: " + game.enemyHP, labelStyle);


       TextButton attackButton = new TextButton("Atacar", textButtonStyle);
        attackButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!game.enemyDefeated) {
                    // NOVO: Prepara uma pergunta aleatória antes de mudar de tela
                    game.prepareNewQuestion();
                    game.setScreen(game.questionScreen);
                }
            }
        });
        table.add(playerHpLabel).pad(10).row();
        table.add(enemyHpLabel).pad(10).row();
        table.add(attackButton).pad(20);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        playerHpLabel.setText("Player HP: " + game.playerHP);
        enemyHpLabel.setText("Enemy HP: " + game.enemyHP);

        if (game.lastAnswerCorrect && !game.enemyDefeated) {
            System.out.println("Player ataca!");
            game.enemyHP -= 20;
            enemyHpLabel.setText("Enemy HP: " + game.enemyHP);
            game.lastAnswerCorrect = false;

            if (game.enemyHP <= 0) {
                game.enemyHP = 0;
                enemyHpLabel.setText("Enemy HP: " + game.enemyHP);
                game.enemyDefeated = true;
                System.out.println("Inimigo derrotado!");
                // Poderia ter um delay antes de mudar de tela, usando Actions por exemplo
                // Gdx.app.postRunnable(() -> game.setScreen(game.explorationScreen)); // Para mudar de tela após um tempo
                game.setScreen(game.explorationScreen); // Imediato por enquanto
            }
        } else if (!game.lastAnswerCorrect && game.cameFromQuestionScreenWithError()) { // Método hipotético em MyGdxGame
            System.out.println("Ataque falhou (errou pergunta)!");
             game.clearCameFromQuestionScreenFlag(); // Limpa a flag
        }
    }

    private void drawWithBorderCombat(Texture texture, float x, float y, float width, float height,
                                     boolean flipX, boolean flipY, Color borderColor, float borderThickness) {
        Color originalColor = game.batch.getColor().cpy();
        game.batch.setColor(borderColor);

        int srcX = 0;
        int srcY = 0;
        int srcWidth = texture.getWidth();
        int srcHeight = texture.getHeight();

        float[] offsets = {
            -borderThickness, -borderThickness, borderThickness, -borderThickness,
            -borderThickness, borderThickness, borderThickness, borderThickness,
            -borderThickness, 0, borderThickness, 0,
            0, -borderThickness, 0, borderThickness
        };

        for (int i = 0; i < offsets.length; i += 2) {
            game.batch.draw(texture, x + offsets[i], y + offsets[i+1], width, height, srcX, srcY, srcWidth, srcHeight, flipX, flipY);
        }

        game.batch.setColor(originalColor);
        game.batch.draw(texture, x, y, width, height, srcX, srcY, srcWidth, srcHeight, flipX, flipY);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float charactersXPos = 50; // Posição X base do jogador
        float enemyXPos = Gdx.graphics.getWidth() - (enemyTexture.getWidth() * COMBAT_SPRITE_SCALE_FACTOR) - 50; // Inimigo à direita
         // Se usar enemyXPos = charactersXPos * 5, pode sair da tela se a tela for pequena

        float playerRenderWidth = playerTexture.getWidth() * COMBAT_SPRITE_SCALE_FACTOR;
        float playerRenderHeight = playerTexture.getHeight() * COMBAT_SPRITE_SCALE_FACTOR;
        float enemyRenderWidth = enemyTexture.getWidth() * COMBAT_SPRITE_SCALE_FACTOR;
        float enemyRenderHeight = enemyTexture.getHeight() * COMBAT_SPRITE_SCALE_FACTOR;

        // Para a tela de combate, assumimos que o jogador está sempre virado para o inimigo (direita)
        // e o inimigo para o jogador (esquerda), ou você pode ter sprites específicos.
        // Por simplicidade, vamos assumir que o player_topdown.png está para direita
        // e o enemy.png está para esquerda por padrão.
        boolean playerFlipX = false; // Assumindo que player_topdown.png olha para direita
        boolean enemyFlipX = false;  // Assumindo que enemy.png olha para esquerda

        // Se o seu enemy.png olha para a direita, você faria:
        // boolean enemyFlipX = true;


        game.batch.begin();
        // Desenhar jogador com borda
        drawWithBorderCombat(playerTexture,
                charactersXPos,
                100,
                playerRenderWidth,
                playerRenderHeight,
                playerFlipX, false, // Sem flip Y
                BORDER_COLOR_COMBAT, BORDER_THICKNESS_COMBAT);


        if (!game.enemyDefeated) {
            // Desenhar inimigo com borda
            drawWithBorderCombat(enemyTexture,
                    enemyXPos, // Usar enemyXPos calculado
                    100,
                    enemyRenderWidth,
                    enemyRenderHeight,
                    enemyFlipX, false, // Sem flip Y
                    BORDER_COLOR_COMBAT, BORDER_THICKNESS_COMBAT);
        }
        game.batch.end();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }
    @Override public void dispose() { stage.dispose(); if (skin != null) skin.dispose(); }
}
