package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class QuestionScreen implements Screen {
    final MyGdxGame game;
    private Stage stage;
    private Skin skin;

    public QuestionScreen(final MyGdxGame game) {
        this.game = game;
        stage = new Stage(new ScreenViewport());
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = game.font;
        Label.LabelStyle labelStyle = new Label.LabelStyle(game.font, com.badlogic.gdx.graphics.Color.WHITE);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label questionLabel = new Label(game.currentQuestion.text, labelStyle);
        table.add(questionLabel).colspan(game.currentQuestion.options.length).pad(20).row();

        for (int i = 0; i < game.currentQuestion.options.length; i++) {
            final int optionIndex = i;
            TextButton optionButton = new TextButton(game.currentQuestion.options[i], textButtonStyle);
            optionButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    game.lastAnswerCorrect = (optionIndex == game.currentQuestion.correctAnswerIndex);
                    if (!game.lastAnswerCorrect) {
                        game.setReturnedFromQuestionScreenWithWrongAnswer(true); // Define a flag se errou
                    } else {
                        game.setReturnedFromQuestionScreenWithWrongAnswer(false); // Garante que está false se acertou
                    }
                    game.setScreen(game.combatScreen);
                }
            });
            table.add(optionButton).pad(10);
        }
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        // É uma boa prática resetar a flag aqui, caso o jogador entre na QuestionScreen
        // por um caminho inesperado (embora no fluxo atual não deva acontecer).
        // game.clearCameFromQuestionScreenFlag(); // Ou resetar apenas se não for via combate normal
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.8f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }
    @Override public void dispose() { stage.dispose(); if (skin != null) skin.dispose(); }
}
