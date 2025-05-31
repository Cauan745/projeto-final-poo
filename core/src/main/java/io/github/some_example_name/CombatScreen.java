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
    private Skin skin; // Opcional
    private Texture playerTexture;
    private Texture enemyTexture;
    private Label playerHpLabel;
    private Label enemyHpLabel;

    public CombatScreen(final MyGdxGame game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());

        playerTexture = game.assetManager.get("player_topdown.png", Texture.class);

        enemyTexture = game.assetManager.get("enemy.png", Texture.class);

        // skin = game.assetManager.get("uiskin.json", Skin.class);
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = game.font;

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.font;
        labelStyle.fontColor = Color.WHITE; // Defina uma cor para o texto do label


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
        // Atualizar HP quando a tela é mostrada (após responder pergunta)
        playerHpLabel.setText("Player HP: " + game.playerHP);
        enemyHpLabel.setText("Enemy HP: " + game.enemyHP);

        if (game.lastAnswerCorrect && !game.enemyDefeated) { // Se voltou da pergunta e acertou
            System.out.println("Player ataca!");
            game.enemyHP -= 20; // Dano simples
            enemyHpLabel.setText("Enemy HP: " + game.enemyHP);
            game.lastAnswerCorrect = false; // Reseta flag

            if (game.enemyHP <= 0) {
                game.enemyHP = 0;
                enemyHpLabel.setText("Enemy HP: " + game.enemyHP);
                game.enemyDefeated = true;
                System.out.println("Inimigo derrotado!");
                // Poderia ter um delay antes de mudar de tela
                game.setScreen(game.explorationScreen);
            }
        } else if (!game.lastAnswerCorrect && Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ANY_KEY) /* Hack para saber se voltou de QuestionScreen errando */) {
            // Se errou a pergunta, poderia ter uma penalidade ou só não fazer nada
            System.out.println("Ataque falhou (errou pergunta)!");
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1); // Vermelho
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float charactersXPos = 50;

        // Desenhar sprites
        game.batch.begin();
        game.batch.draw(playerTexture, charactersXPos, 100);


        if (!game.enemyDefeated) {
            game.batch.draw(enemyTexture, charactersXPos*5, 100);
        }
        game.batch.end();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }
    // ... hide, resize, pause, resume, dispose ...
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }
    @Override public void dispose() { stage.dispose(); if (skin != null) skin.dispose(); }
}
